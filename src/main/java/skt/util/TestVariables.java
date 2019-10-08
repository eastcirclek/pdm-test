package skt.util;

public class TestVariables {
    public static final int windowSize = 100;
    public static final int triggerSize = 1;

    public static final long timeLag = 3000; //milliseconds

    public static final long dataInterval = 100; // milliseconds

    public static final long distanceTreshold = 500;

    public static final int randomStart = -30;
    public static final int randomEnd = 100;
    public static final int randomRange = randomEnd -randomStart + 1;

    public static final int anomalyValue = 1000;
    public static final int anomalyPredictionDataInterval = 200;

    public static final int outlierRange = 98;

    public static final String rootPath = System.getProperty("user.dir") + "/data/";
    public static final String inputPath = rootPath + "inputData.txt";
    public static final String outlierPath = rootPath + "outlierData.txt";
    public static final String predictionPath = rootPath + "predictionData.txt";
    public static final String scorePath = rootPath + "scoreData.txt";
}