package skt.model.execution.impl;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import skt.model.ModelExecutionService;
import skt.util.SensorData;
import skt.util.TestVariables;

import java.util.Iterator;

public class JavaAPIExecution implements ModelExecutionService{
    private static Session sess = null;

    public JavaAPIExecution() {
            try {
                SavedModelBundle b = SavedModelBundle.load(TestVariables.modelPath, "serve"); //restore model
                sess = b.session(); //get session
            } catch (Exception e) {
                System.out.println(e);
                System.exit(1); //exit system if model is not available
            }
    }

    public SensorData executeModel(Iterable<SensorData> dataInWindow, int dataId, long timestamp) {
        float[][] input = new float[TestVariables.numberOfFeature][TestVariables.windowSize];
        Iterator<SensorData> iter = dataInWindow.iterator();

        for (int i =0; i < TestVariables.windowSize;i++) {
            SensorData curData = iter.next();
            input[0][i] = (float) curData.getTemperature();
            input[1][i] = (float) curData.getHumidity();
            input[2][i] = (float) curData.getMoisture();
            input[3][i] = (float) curData.getVibration();
            input[4][i] = (float) curData.getPressure();
        }

        Tensor featureVectors = Tensor.create(input);
        float[][] predictedVector = sess.runner().feed("input", featureVectors)
                .fetch("output").run().get(0).copyTo(new float[TestVariables.numberOfFeature][1]);

        double predictedTemperature = predictedVector[0][0];
        double predictedHumidity = predictedVector[1][0];
        double predictedMoisture = predictedVector[2][0];
        double predictedVibration = predictedVector[3][0];
        double predictedPressure = predictedVector[4][0];

        SensorData predictedSensorData = new SensorData(dataId,predictedTemperature,predictedHumidity,predictedMoisture,
                predictedVibration,predictedPressure,timestamp);

        return predictedSensorData;
    }
}