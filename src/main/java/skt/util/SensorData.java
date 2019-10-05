package skt.util;

public class SensorData {
    private final int dataId;
    private final double temperature;
    private final double humidity;
    private final double moisture;
    private final double vibration;
    private final double pressure;
    private final long timestamp;

    public SensorData(
            int dataId,
            double temperature,
            double humidity,
            double moisture,
            double vibration,
            double pressure,
            long timestamp
    ) {

        this.temperature = temperature;
        this.humidity = humidity;
        this.moisture = moisture;
        this.vibration = vibration;
        this.pressure = pressure;
        this.timestamp = timestamp;
        this.dataId = dataId;
    }
    public double getTemperature() {
        return temperature;
    }
    public double getHumidity() {
        return humidity;
    }
    public double getMoisture() {
        return moisture;
    }
    public double getVibration() {
        return vibration;
    }
    public double getPressure() { return pressure; }
    public double[] getFeatureVector() {
        return new double[]{temperature,humidity,moisture,vibration,pressure};
    }
    public long getTimestamp() {
        return timestamp;
    }
    public int getDataId() { return dataId; }

}