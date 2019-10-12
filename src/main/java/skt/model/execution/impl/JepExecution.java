package skt.model.execution.impl;

import jep.*;
import skt.model.ModelExecutionService;
import skt.util.SensorData;
import skt.util.TestVariables;

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
                interpeter.set("serve",TestVariables.modelTag);
                interpeter.exec("tf.saved_model.loader.load(sess,[serve],model_path)");
            } catch (Exception e) {
                System.out.println(e);
                System.exit(1);
            }
        }
    }

    public SensorData executeModel(Iterable<SensorData> dataInWindow, int dataId, long timestamp) {
        float[] values = makeOneDimensionArray(dataInWindow);

        try {
            interpeter.set("input", TestVariables.modelInputName + ":0");
            interpeter.set("output", TestVariables.modelOutputName + ":0");
            NDArray<float[]> valuesForJep = new NDArray<>(values,5,100);
            interpeter.set("x", valuesForJep);
            interpeter.exec("prediction = sess.run(output, feed_dict={input: x})");
            NDArray<float[]> predictedValues = (NDArray<float[]>)interpeter.getValue("prediction");

            double predictedTemperature = predictedValues.getData()[0];
            double predictedHumidity = predictedValues.getData()[1];
            double predictedMoisture = predictedValues.getData()[2];
            double predictedVibration = predictedValues.getData()[3];
            double predictedPressure = predictedValues.getData()[4];

            SensorData predictedSensorData = new SensorData(dataId, predictedTemperature, predictedHumidity,
                    predictedMoisture, predictedVibration, predictedPressure, timestamp);

            return predictedSensorData;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}