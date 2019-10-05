package skt.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SinkFunction {
    private static SinkFunction sinkFunction = null;
    private static final String rootPath = System.getProperty("user.dir") + "/data/";


    public static SinkFunction getSinkFunction(){
        if (sinkFunction == null) {
            sinkFunction = new SinkFunction();
            File dataDirectory = new File(rootPath);

            if (dataDirectory.exists()) { // Remove directory containing data produced in previous execution
                try {
                    while (dataDirectory.exists()) {
                        File[] itemList = dataDirectory.listFiles();

                        for (int i = 0; i < itemList.length; i++) {
                            itemList[i].delete();
                        }

                        if (itemList.length == 0 && dataDirectory.isDirectory()) {
                            dataDirectory.delete();
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            dataDirectory.mkdirs();
        }
        return sinkFunction;
    }

    public void scoreSink(SensorData measurementData, SensorData predictionData, double distance) {
        String filePath = (rootPath + "scoreData.txt");
        File file = new File(filePath);
        printToFile(file,measurementData,predictionData,distance);
    }

    public void predictionSink(SensorData sensorData) {
        String filePath = (rootPath + "preidctionData.txt");
        File file = new File(filePath);
        printToFile(file,sensorData);
    }

    public void outlierSink(SensorData sensorData) {
        String filePath = (rootPath + "outlierData.txt");
        File file = new File(filePath);
        printToFile(file,sensorData);
    }

    public void inputSink(SensorData sensorData) {
        String filePath = (rootPath + "inputData.txt");
        File file = new File(filePath);
        printToFile(file,sensorData);
    }

    private void printToFile (File file, SensorData measurementData, SensorData predictionData, double distance) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true));

            String output = String.format("[%d : %d] [%d : %d] [%.2f,%.2f,%.2f,%.2f,%.2f : %.2f,%.2f,%.2f,%.2f,%.2f] " +
                    "=> %f",
                    measurementData.getDataId(), predictionData.getDataId(),
                    measurementData.getTimestamp(), predictionData.getTimestamp(),
                    measurementData.getTemperature(), measurementData.getHumidity(), measurementData.getMoisture(),
                    measurementData.getVibration(), measurementData.getPressure(),
                    predictionData.getTemperature(), predictionData.getHumidity(), predictionData.getMoisture(),
                    predictionData.getVibration(), predictionData.getPressure(),
                    distance);

            if (file.isFile() && file.canWrite()) {
                bufferedWriter.write(output);
                bufferedWriter.newLine();
                bufferedWriter.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void printToFile (File file, SensorData sensorData) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true));
            String output = String.format("%d, %d, [%.2f,%.2f,%.2f,%.2f,]",
                    sensorData.getDataId(), sensorData.getTimestamp(),
                    sensorData.getTemperature(),sensorData.getHumidity(),
                    sensorData.getMoisture(),sensorData.getVibration(),
                    sensorData.getPressure()
            );

            if (file.isFile() && file.canWrite()) {
                bufferedWriter.write(output);
                bufferedWriter.newLine();
                bufferedWriter.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}