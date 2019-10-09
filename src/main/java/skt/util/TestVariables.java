package skt.util;

public class TestVariables {
    public static final int windowSize = 100;
    public static final int triggerSize = 1;

    public static final long timeLag = 3000; //milliseconds

    public static final long dataInterval = 100; // milliseconds

    public static final long distanceTreshold = 41;

    public static final int randomStart = 1;
    public static final int randomEnd = 45;
    public static final int randomRange = randomEnd -randomStart + 1;

    public static final int anomalyValue = 1000;
    public static final int anomalyPredictionDataInterval = 200;

    public static final int outlierRange = 98;

    public static final String rootPath = System.getProperty("user.dir");
    public static final String modelPath = rootPath + "/model/";
    public static final String dataPath = rootPath + "/data/";
    public static final String inputPath = dataPath + "inputData.txt";
    public static final String outlierPath = dataPath + "outlierData.txt";
    public static final String predictionPath = dataPath + "predictionData.txt";
    public static final String scorePath = dataPath + "scoreData.txt";
}