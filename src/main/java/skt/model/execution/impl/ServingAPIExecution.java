package skt.model.execution.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import skt.model.ModelExecutionService;
import skt.util.SensorData;
import com.google.protobuf.Int64Value;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import org.tensorflow.framework.TensorShapeProto.Dim;
import skt.util.TestVariables;
import tensorflow.serving.Predict;
import tensorflow.serving.Predict.PredictRequest;
import tensorflow.serving.Predict.PredictResponse;
import tensorflow.serving.PredictionServiceGrpc;
import tensorflow.serving.PredictionServiceGrpc.PredictionServiceBlockingStub;



public class ServingAPIExecution implements ModelExecutionService {
    private static PredictionServiceBlockingStub stub = null;
    private static PredictRequest.Builder requestBuilder = null;

    public ServingAPIExecution() {
        ManagedChannel channel = ManagedChannelBuilder.
                forAddress(TestVariables.servingMachineIP, TestVariables.servingMachinePort).
                usePlaintext().build();
        stub = PredictionServiceGrpc.newBlockingStub(channel);

        requestBuilder = Predict.PredictRequest.newBuilder();

        // create ModelSpec
        tensorflow.serving.Model.ModelSpec.Builder modelSpecBuilder = tensorflow.serving.Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName(TestVariables.servingModelName);
        modelSpecBuilder.setVersion(Int64Value.of(TestVariables.servingModelVersion));
        modelSpecBuilder.setSignatureName(TestVariables.servingModelSignature);
        requestBuilder.setModelSpec(modelSpecBuilder);
    }
    public SensorData executeModel(Iterable<SensorData> dataInWindow, int dataId, long timestamp) {
        float[] values = makeOneDimensionArray(dataInWindow);

        TensorProto.Builder featuresTensorBuilder = TensorProto.newBuilder();
        featuresTensorBuilder.setDtype(org.tensorflow.framework.DataType.DT_FLOAT);

        for (int i = 0; i < values.length; ++i) {
                featuresTensorBuilder.addFloatVal(values[i]);
        }

        Dim dimo = TensorShapeProto.Dim.newBuilder().setSize(TestVariables.numberOfFeature).build();
        Dim dimt = TensorShapeProto.Dim.newBuilder().setSize(TestVariables.windowSize).build();
        TensorShapeProto shape = TensorShapeProto.newBuilder().addDim(dimo).addDim(dimt).build();
        featuresTensorBuilder.setTensorShape(shape);
        TensorProto featuresTensorProto = featuresTensorBuilder.build();

        requestBuilder.putInputs(TestVariables.modelInputName, featuresTensorProto);

        PredictRequest request = requestBuilder.build();
        PredictResponse response = stub.predict(request);
        String[] splited = response.getOutputsMap().toString().split("float_val: ");


        double predictedTemperature = Double.valueOf(splited[1]);
        double predictedHumidity = Double.valueOf(splited[2]);
        double predictedMoisture = Double.valueOf(splited[3]);
        double predictedVibration = Double.valueOf(splited[4]);

        double predictedPressure = Double.valueOf(splited[5].split("}")[0]);
        SensorData predictedSensorData = new SensorData(dataId, predictedTemperature, predictedHumidity, predictedMoisture,
                predictedVibration, predictedPressure, timestamp);

        return predictedSensorData;
    }
}