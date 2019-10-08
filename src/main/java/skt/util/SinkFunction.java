package skt.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SinkFunction {
    private static SinkFunction sinkFunction = null;

    public static SinkFunction getSinkFunction(){
        if (sinkFunction == null) {
            sinkFunction = new SinkFunction();
            File dataDirectory = new File(TestVariables.rootPath);

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

    public void sink(SensorData measurementData, SensorData predictionData, double distance, File file) {
        writeStringToFile(file,dataToOutputString(measurementData,predictionData, distance));
    }

    public void sink(SensorData sensorData, File file) {
        writeStringToFile(file,dataToOutputString(sensorData));
    }

    public void sink(String output, File file) {
        writeStringToFile(file,output);
    }

    public String dataToOutputString(SensorData sensorData) {
        String output = String.format("%d, %d, [%.2f,%.2f,%.2f,%.2f,]",
                sensorData.getDataId(), sensorData.getTimestamp(),
                sensorData.getTemperature(),sensorData.getHumidity(),
                sensorData.getMoisture(),sensorData.getVibration(),
                sensorData.getPressure()
        );
        return output;
    }

    public String dataToOutputString(SensorData measurementData, SensorData predictionData, double distance) {
        String output = String.format("[%d : %d] [%d : %d] [%.2f,%.2f,%.2f,%.2f,%.2f : %.2f,%.2f,%.2f,%.2f,%.2f] " +
                        "=> %f",
                measurementData.getDataId(), predictionData.getDataId(),
                measurementData.getTimestamp(), predictionData.getTimestamp(),
                measurementData.getTemperature(), measurementData.getHumidity(), measurementData.getMoisture(),
                measurementData.getVibration(), measurementData.getPressure(),
                predictionData.getTemperature(), predictionData.getHumidity(), predictionData.getMoisture(),
                predictionData.getVibration(), predictionData.getPressure(),
                distance);
        return output;
    }

    private void writeStringToFile (File file, String output) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true));
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