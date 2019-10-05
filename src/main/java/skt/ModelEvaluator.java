package skt;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.evictors.CountEvictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import skt.util.*;

import java.util.stream.StreamSupport;

public class ModelEvaluator {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<SensorData> originStream = streamEnv.addSource(new DataGenerator());
        DataStream<SensorData> filteredStream = originStream.process(new OutlierProcessingFunction());

        DataStream<SensorData> measurementStream = filteredStream
                .windowAll(GlobalWindows.create())
                .trigger(CountTrigger.of(TestVariables.triggerSize))
                .evictor(CountEvictor.of(TestVariables.windowSize))
                .apply(new MeasurementWindowFunction())
                .assignTimestampsAndWatermarks(new MeasurementTimestampAndWaterMarkAssigner());

        DataStream<SensorData> predictionStream = filteredStream
                .windowAll(GlobalWindows.create())
                .trigger(CountTrigger.of(TestVariables.triggerSize))
                .evictor(CountEvictor.of(TestVariables.windowSize))
                .apply(new SensorWindowFunction())
                .assignTimestampsAndWatermarks(new PredictionTimestampAndWaterMarkAssigner());


        DataStream<Tuple3<SensorData, SensorData, Double>> scoreStream = measurementStream
                .join(predictionStream)
                .where(new KeySelectFunction())
                .equalTo(new KeySelectFunction())
                .window(TumblingEventTimeWindows.of(Time.milliseconds(TestVariables.dataInterval)))
                .apply(new StreamJoinFunction());

        scoreStream.process(new ScoreProcessingFunction());

        streamEnv.execute();
    }

    static class StreamJoinFunction implements JoinFunction<SensorData, SensorData, Tuple3<SensorData, SensorData, Double>> {
        @Override
        public Tuple3<SensorData, SensorData, Double> join(SensorData measurementData, SensorData predictionData) throws Exception {
            double distance = calculateDistance(measurementData,predictionData);

            return new Tuple3<>(measurementData,predictionData,distance);
        }

        private double calculateDistance(SensorData measurementData, SensorData predictionData) {
            double distance;
            double[] measurement = measurementData.getFeatureVector();
            double[] prediction = predictionData.getFeatureVector();
            EuclideanDistance edo = new EuclideanDistance();
            distance = edo.compute(measurement,prediction);

            return distance;
        }
    }

    static class ScoreProcessingFunction extends ProcessFunction<Tuple3<SensorData, SensorData, Double>, SensorData> {
        static final String ANSI_RED = "\u001B[31m";
        static final String ANSI_RESET = "\u001B[0m";
        static final String ANSI_BLUE = "\u001B[34m";

        @Override
        public void processElement(Tuple3<SensorData, SensorData, Double> data, Context context, Collector<SensorData> collector) throws Exception {
            if (data.f2 > TestVariables.distanceTreshold) {
                generateAlaram(data.f0,data.f1,data.f2);
            }

            SinkFunction sinkFunction = SinkFunction.getSinkFunction();
            sinkFunction.scoreSink(data.f0,data.f1,data.f2);
        }

        private void generateAlaram(SensorData measurementData, SensorData predictionData, Double distance) {
            System.out.println(ANSI_RED + "[WARNING] "+ ANSI_BLUE + "Distance between two feature vectors is outside the allowable range.");
            String output = String.format("[%d : %d] [%.2f,%.2f,%.2f,%.2f,%.2f : %.2f,%.2f,%.2f,%.2f,%.2f] " + "=> %f",
                    measurementData.getDataId(), predictionData.getDataId(),
                    measurementData.getTemperature(), measurementData.getHumidity(), measurementData.getMoisture(),
                    measurementData.getVibration(), measurementData.getPressure(),
                    predictionData.getTemperature(), predictionData.getHumidity(), predictionData.getMoisture(),
                    predictionData.getVibration(), predictionData.getPressure(),
                    distance);
            System.out.println(ANSI_RESET + output);
        }
    }

    static class OutlierProcessingFunction extends ProcessFunction<SensorData, SensorData> {
        @Override
        public void processElement(SensorData sensorData, Context context, Collector<SensorData> collector) throws Exception {
            SinkFunction sinkFunction = SinkFunction.getSinkFunction();

            if (sensorData.getPressure() > TestVariables.outlierRange) {
                sinkFunction.outlierSink(sensorData);
            } else {
                sinkFunction.inputSink(sensorData);
                collector.collect(sensorData);
            }
        }
    }

    static class PredictionTimestampAndWaterMarkAssigner implements AssignerWithPeriodicWatermarks<SensorData> {

        private final long maxTimeLag = TestVariables.timeLag;
        private long currentMaxTimestamp;
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimestamp-maxTimeLag);
        }

        @Override
        public long extractTimestamp(SensorData sensorData, long l) {
            long timestamp = sensorData.getTimestamp();
            currentMaxTimestamp = Math.max(timestamp,currentMaxTimestamp);
            return sensorData.getTimestamp();
        }
    }

    static class MeasurementTimestampAndWaterMarkAssigner implements AssignerWithPeriodicWatermarks<SensorData> {
        private long currentMaxTimestamp;

        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimestamp);
        }

        @Override
        public long extractTimestamp(SensorData sensorData, long l) {
            long timestamp = sensorData.getTimestamp();
            currentMaxTimestamp = Math.max(timestamp,currentMaxTimestamp);
            return sensorData.getTimestamp();
        }
    }

    static class KeySelectFunction implements KeySelector<SensorData, Long> {
        @Override
        public Long getKey(SensorData sensorData) throws Exception {
            return sensorData.getTimestamp();
        }
    }

    static class MeasurementWindowFunction implements AllWindowFunction<SensorData, SensorData, GlobalWindow> {
        @Override
        public void apply(GlobalWindow globalWindow, Iterable<SensorData> dataInWindow, Collector<SensorData> collector) throws Exception {
            if (StreamSupport.stream(dataInWindow.spliterator(), false).count() == TestVariables.windowSize) {
                SensorData latestData = dataInWindow.iterator().next();
                collector.collect(latestData);
            }
        }
    }

    static class SensorWindowFunction implements AllWindowFunction<SensorData, SensorData, GlobalWindow> {
        @Override
        public void apply(GlobalWindow globalWindow, Iterable<SensorData> dataInWindow, Collector<SensorData> collector) throws Exception {
            if (StreamSupport.stream(dataInWindow.spliterator(), false).count() == TestVariables.windowSize) {
                SensorData predictedSensorData = rnnModel(dataInWindow);

                SinkFunction sinkFunction = SinkFunction.getSinkFunction();
                sinkFunction.predictionSink(predictedSensorData);

                collector.collect(predictedSensorData);
            }
        }
        private SensorData rnnModel (Iterable<SensorData> dataInWindow) {
            SensorData latestData = dataInWindow.iterator().next();
            RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator();
            int dataId = latestData.getDataId();
            SensorData predictedSensorData;

            if ((dataId % TestVariables.anomalyPredictionDataInterval) == 0 ) { // Generate feature vector for alarming test
                predictedSensorData = randomGenerator.generateRandomSensorData(latestData.getDataId(),
                        latestData.getTimestamp() + TestVariables.timeLag, false);
            }
            else {
                predictedSensorData = randomGenerator.generateRandomSensorData(latestData.getDataId(),
                        latestData.getTimestamp() + TestVariables.timeLag, true);
            }
            return predictedSensorData;
        }

    }
}