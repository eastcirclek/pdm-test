package skt.util;

public class SensorData implements Cloneable{
    private final int dataId;
    private final double temperature;
    private final double humidity;
    private final double moisture;
    private final double vibration;
    private final double pressure;
    private final long timestamp;
    private int partition;

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
        this.partition = -1;
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
    public void setPartition(int partitionNumber) { this.partition = partitionNumber;}
    public int getPartition() {return partition;}
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        } catch (Exception e) {}
        return clone;
    }

}