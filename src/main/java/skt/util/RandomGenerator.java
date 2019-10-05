package skt.util;

import java.util.Random;

public class RandomGenerator {
    private static RandomGenerator randomGenerator = null;

    public static RandomGenerator getRandomGenerator(){ //Singleton pattern
        if (randomGenerator == null) {
            randomGenerator = new RandomGenerator();
        }
        return randomGenerator;
    }

    public SensorData generateRandomSensorData(int dataId, long timeStamp,boolean flag) {
        double temperature = getRandom();
        double humidity = getRandom();
        double moisture = getRandom();
        double vibration = getRandom();
        double pressure = getRandom();

        if (flag == false) {
            temperature += TestVariables.anomalyValue;
            humidity += TestVariables.anomalyValue;
            moisture += TestVariables.anomalyValue;
            vibration += TestVariables.anomalyValue;
            pressure += TestVariables.anomalyValue;
        }

        return new SensorData(dataId,temperature,humidity,moisture,vibration,pressure,timeStamp);
    }

    private double getRandom() {
        Random randomGenerator = new Random();
        int start = TestVariables.randomStart;
        int end = TestVariables.randomEnd;
        double range = TestVariables.randomRange;

        double randomVal = randomGenerator.nextDouble() * range + start;
        return randomVal;
    }
}