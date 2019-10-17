package skt.util;

public class TestVariables {
    public static final int windowSize = 100;
    public static final int triggerSize = 1;
    public static final long timeLag = 3000; //milliseconds
    public static final long dataInterval = 100; // milliseconds
    public static final long allowableLateness = dataInterval * 2;

    public static final long distanceTreshold = 41;

    public static final int randomStart = 1;
    public static final int randomEnd = 45;
    public static final int randomRange = randomEnd -randomStart + 1;

    public static final int outlierRange = 42;

    public static final int numberOfFeature = 5;
    public static final int numberOfPartition = 1;

    public enum ExecutionMode {
        JAVAAPI, JEP, SERVINGAPI
    }
    public static final ExecutionMode currentExecutionMode = ExecutionMode.JAVAAPI; // Set execution mode

    //For serving API execution mode
    public static final String servingMachineIP = "0.0.0.0";
    public static final int servingMachinePort = 9000;
    public static final String servingModelName = "DNN";
    public static final int servingModelVersion = 1;
    public static final String servingModelSignature = "serving_default";

    //For general execution mode
    public static final String modelInputName = "input";
    public static final String modelOutputName = "output";
    public static final String modelTag = "serve";

    public static final String rootPath = System.getProperty("user.dir");
    public static final String modelPath = rootPath + "/model/";
    public static final String dataPath = rootPath + "/data/";
    public static final String inputPath = dataPath + "inputData.txt";
    public static final String outlierPath = dataPath + "outlierData.txt";
    public static final String predictionPath = dataPath + "predictionData.txt";
    public static final String scorePath = dataPath + "scoreData.txt";
    public static final String jepLibraryPath = rootPath + "/lib/jep/jep.so";
}