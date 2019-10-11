package skt.model.execution.impl;

import jep.*;
import skt.model.ModelExecutionService;
import skt.util.SensorData;
import skt.util.TestVariables;

import java.util.Iterator;

public class JepExecution implements ModelExecutionService {
    private static Interpreter interpeter = null;

    public JepExecution() {
        if (interpeter == null) {
            try {
                MainInterpreter.setJepLibraryPath(TestVariables.jepLibraryPath);
                interpeter = new SharedInterpreter();
                interpeter.exec("import tensorflow as tf");
                interpeter.exec("import numpy");
                interpeter.exec("sess = tf.Session()");
                interpeter.set("model_path", TestVariables.modelPath);
                String serving = "serve";
                interpeter.set("serve",serving);
                interpeter.exec("tf.saved_model.loader.load(sess,[serve],model_path)");
            } catch (Exception e) {
                System.out.println("e");
                System.exit(1);
            }
        }
    }

    public SensorData executeModel(Iterable<SensorData> dataInWindow, int dataId, long timestamp) {
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

        float[] values = new float[TestVariables.numberOfFeature*TestVariables.windowSize];
        System.arraycopy(temperature,0,values,0,temperature.length);
        System.arraycopy(humidity,0,values,temperature.length,humidity.length);
        System.arraycopy(moisture,0,values,humidity.length*2,moisture.length);
        System.arraycopy(vibration,0,values,moisture.length*3,vibration.length);
        System.arraycopy(pressure,0,values,vibration.length*4,pressure.length);

        try {
            String in = "input:0";
            String out = "output:0";
            interpeter.set("input", in);
            interpeter.set("output", out);
            NDArray<float[]> valuesForJep = new NDArray<>(values,5,100);
            interpeter.set("x", valuesForJep);
            interpeter.exec("prediction = sess.run(output, feed_dict={input: x})");
            NDArray<float[]> predictedValues = (NDArray<float[]>)interpeter.getValue("prediction");

            double predictedTemperature = predictedValues.getData()[0];
            double predictedHumidity = predictedValues.getData()[1];
            double predictedMoisture = predictedValues.getData()[2];
            double predictedVibration = predictedValues.getData()[3];
            double predictedPressure = predictedValues.getData()[4];

            SensorData predictedSensorData = new SensorData(dataId, predictedTemperature, predictedHumidity, predictedMoisture,
                   predictedVibration, predictedPressure, timestamp);

            return predictedSensorData;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}