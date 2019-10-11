package skt.model;

import skt.util.SensorData;
import skt.util.TestVariables;

import java.util.Iterator;

public interface ModelExecutionService {

    SensorData executeModel(Iterable<SensorData> dataInWindow, int dataId, long timestamp);

    default float[][] makeTwoDimensionArray(Iterable<SensorData> dataInWindow) {
        float[][] array = new float[TestVariables.numberOfFeature][TestVariables.windowSize];
        Iterator<SensorData> iter = dataInWindow.iterator();

        for (int i =0; i < TestVariables.windowSize;i++) {
            SensorData curData = iter.next();
            array[0][i] = (float) curData.getTemperature();
            array[1][i] = (float) curData.getHumidity();
            array[2][i] = (float) curData.getMoisture();
            array[3][i] = (float) curData.getVibration();
            array[4][i] = (float) curData.getPressure();
        }
        return array;
    }

    default float[] makeOneDimensionArray(Iterable<SensorData> dataInWindow) {
        float[][] input = new float[TestVariables.numberOfFeature][TestVariables.windowSize];
        Iterator<SensorData> iter = dataInWindow.iterator();

        float[] temperature = new float[TestVariables.windowSize];
        float[] humidity = new float[TestVariables.windowSize];
        float[] moisture = new float[TestVariables.windowSize];
        float[] vibration = new float[TestVariables.windowSize];
        float[] pressure = new float[TestVariables.windowSize];

        for (int i = 0; i < TestVariables.windowSize; i++) {
            SensorData curData = iter.next();
            temperature[i] = (float) curData.getTemperature();
            humidity[i] = (float) curData.getHumidity();
            moisture[i] = (float) curData.getMoisture();
            vibration[i] = (float) curData.getVibration();
            pressure[i] = (float) curData.getPressure();

        }

        float[] array = new float[TestVariables.numberOfFeature*TestVariables.windowSize];
        System.arraycopy(temperature,0,array,0,temperature.length);
        System.arraycopy(humidity,0,array,temperature.length,humidity.length);
        System.arraycopy(moisture,0,array,humidity.length*2,moisture.length);
        System.arraycopy(vibration,0,array,moisture.length*3,vibration.length);
        System.arraycopy(pressure,0,array,vibration.length*4,pressure.length);
        return array;
    }
}