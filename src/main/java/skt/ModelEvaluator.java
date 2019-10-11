package skt;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.sink.*;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.evictors.CountEvictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.flink.util.OutputTag;
import skt.model.ModelExecutionFactory;
import skt.util.*;
import skt.util.SinkFunction;

import java.io.File;
import java.util.Iterator;
import java.util.stream.StreamSupport;

public class ModelEvaluator {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStream<SensorData> originStream = streamEnv.addSource(new DataGenerator());

        final OutputTag<String> outlierTag = new OutputTag<String>("outlierData"){};
        SingleOutputStreamOperator<SensorData> filteredStream = originStream.process(
                new ProcessFunction<SensorData,SensorData>() {
                    @Override
                    public void processElement(SensorData sensorData, Context ctx, Collector<SensorData> out)
                            throws Exception {
                        if (sensorData.getPressure() > TestVariables.outlierRange) {
                            ctx.output(outlierTag, SinkFunction.getSinkFunction().dataToOutputString(sensorData));
                        } else {
                            out.collect(sensorData);
                        }
                    }
                });
        filteredStream.addSink(new InputSink());        // InputSink

        DataStream<String> outlierStream = filteredStream.getSideOutput(outlierTag);
        outlierStream.addSink(new OutlierSink());       // Outlier Sink

        DataStream<SensorData> measurementStream = filteredStream
                .windowAll(GlobalWindows.create())
                .trigger(CountTrigger.of(TestVariables.triggerSize))
                .evictor(CountEvictor.of(TestVariables.windowSize))
                .apply(new MeasurementWindowFunction())
                .assignTimestampsAndWatermarks(new MeasurementTimestampAndWaterMarkAssigner());


        DataStream<SensorData> predictionStream = filteredStream
                .flatMap(new ReplicaFunction())
                .setParallelism(TestVariables.numberOfPartition)
                .keyBy(new PartitionKeySelectionFunction())
                .windowAll(GlobalWindows.create())
                .trigger(CountTrigger.of(TestVariables.triggerSize))
                .evictor(CountEvictor.of(TestVariables.windowSize))
                .apply(new SensorWindowFunction())
                .assignTimestampsAndWatermarks(new PredictionTimestampAndWaterMarkAssigner())
                .setParallelism(1)
                .windowAll(TumblingEventTimeWindows.of(Time.milliseconds(TestVariables.dataInterval)))
                .apply(new AverageWindowFunction());
        predictionStream.addSink(new PredictionSink());

        DataStream<Tuple3<SensorData, SensorData, Double>> scoreStream = measurementStream
                .join(predictionStream)
                .where(new TimestampKeySelectFunction())
                .equalTo(new TimestampKeySelectFunction())
                .window(TumblingEventTimeWindows.of(Time.milliseconds(TestVariables.dataInterval)))
                .apply(new StreamJoinFunction());
        scoreStream.addSink(new ScoreSink());           // Score Sink

        scoreStream.process(new ScoreProcessingFunction());

        streamEnv.execute();
    }

    static class AverageWindowFunction implements AllWindowFunction<SensorData,SensorData,TimeWindow> {

        @Override
        public void apply(TimeWindow window, Iterable<SensorData> dataInWindow, Collector<SensorData> out) throws Exception {
            SensorData averageSensorData = getAverageSensorData(dataInWindow);
            if (averageSensorData != null) {
                out.collect(averageSensorData);
            }
        }

        private SensorData getAverageSensorData(Iterable<SensorData> dataInWindow) {
            long windowLength = StreamSupport.stream(dataInWindow.spliterator(), false).count();

            Double[][] predictedValues = new Double[TestVariables.numberOfFeature][TestVariables.numberOfPartition];
            int dataId = -1;
            long timeStamp = -1;
            int curPoint = 0;

            if (windowLength == TestVariables.numberOfPartition) {
                Iterator<SensorData> iter = dataInWindow.iterator();
                while (iter.hasNext()) {
                    SensorData sensorData = iter.next();
                    dataId = sensorData.getDataId();
                    timeStamp = sensorData.getTimestamp();
                    predictedValues[0][curPoint] = sensorData.getTemperature();
                    predictedValues[1][curPoint] = sensorData.getHumidity();
                    predictedValues[2][curPoint] = sensorData.getMoisture();
                    predictedValues[3][curPoint] = sensorData.getVibration();
                    predictedValues[4][curPoint] = sensorData.getPressure();

                    curPoint++;
                }
            }

            Double[] averageValues = new Double[TestVariables.numberOfFeature];
            double sum = 0;
            for (int i = 0; i < TestVariables.numberOfFeature; i++) {
                for (int j = 0; j < TestVariables.numberOfPartition; j++) {
                    sum += predictedValues[i][j];
                }
                averageValues[i] = sum / TestVariables.numberOfPartition;
                sum = 0;
            }

            return new SensorData(dataId, averageValues[0], averageValues[1], averageValues[2], averageValues[3],
                    averageValues[4], timeStamp);
        }
    }

    static class ReplicaFunction implements FlatMapFunction <SensorData, SensorData> {

        @Override
        public void flatMap(SensorData originSensorData, Collector<SensorData> out) throws Exception {
            originSensorData.setPartition(0);
            out.collect(originSensorData);

            for (int i =1 ; i < TestVariables.numberOfPartition; i++) {
                SensorData cloneSensorData = (SensorData) originSensorData.clone();
                cloneSensorData.setPartition(i);
                out.collect(cloneSensorData);
            }
        }
    }

    static class PartitionKeySelectionFunction implements KeySelector<SensorData, Integer> {
        @Override
        public Integer getKey(SensorData sensorData) throws Exception {
            return sensorData.getPartition();
        }
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

    static class TimestampKeySelectFunction implements KeySelector<SensorData, Long> {
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
                SensorData latestData = dataInWindow.iterator().next();
                int dataId = latestData.getDataId();
                long timeStamp = latestData.getTimestamp() + TestVariables.timeLag;
                SensorData predictedSensorData = ModelExecutionFactory.
                        getModel(TestVariables.currentExecutionMode).
                        executeModel(dataInWindow, dataId, timeStamp);
                collector.collect(predictedSensorData);
            }
        }
    }

    static class InputSink extends RichSinkFunction<SensorData> {
        static final File file = new File(TestVariables.inputPath);
        @Override
        public void invoke(SensorData sensorData, Context context) {
            SinkFunction.getSinkFunction().sink(sensorData, file);
        }
    }
    static class OutlierSink extends RichSinkFunction<String> {
        static final File file = new File(TestVariables.outlierPath);
        @Override
        public void invoke(String sensorData, Context context) {
            SinkFunction.getSinkFunction().sink(sensorData, file);
        }
    }
    static class PredictionSink extends RichSinkFunction<SensorData> {
        static final File file = new File(TestVariables.predictionPath);

        @Override
        public void invoke(SensorData sensorData, Context context) {
            SinkFunction.getSinkFunction().sink(sensorData, file);
        }
    }
    static class ScoreSink extends RichSinkFunction<Tuple3<SensorData,SensorData,Double>> {
        static final File file = new File(TestVariables.scorePath);
        @Override
        public void invoke(Tuple3<SensorData,SensorData,Double> data, Context context) {
            SinkFunction.getSinkFunction().sink(data.f0,data.f1,data.f2, file);
        }
    }
}