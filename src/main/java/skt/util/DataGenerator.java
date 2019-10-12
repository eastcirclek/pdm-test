package skt.util;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.sql.Timestamp;

public class DataGenerator extends RichSourceFunction<SensorData> {
    private volatile boolean isRunning = true;

    @Override
    public void run(SourceContext<SensorData> ctx) throws Exception {
        int dataId = 0;
        long curTimeStamp;

        Timestamp timestampGenerator = new Timestamp(System.currentTimeMillis());
        curTimeStamp = timestampGenerator.getTime();
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator();

        while (isRunning) {
            dataId ++;
            SensorData data;
            curTimeStamp += TestVariables.dataInterval;
            data = randomGenerator.generateRandomSensorData(dataId,curTimeStamp);

            ctx.collectWithTimestamp(data,data.getTimestamp());

            Thread.sleep(TestVariables.dataInterval);
        }
    }

    @Override
    public void cancel() {
            isRunning = false;
        }
 }

