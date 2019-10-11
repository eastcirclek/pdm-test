package skt.model;

import skt.util.SensorData;

public interface ModelExecutionService {

    SensorData executeModel(Iterable<SensorData> dataInWindow, int dataId, long timestamp);
}