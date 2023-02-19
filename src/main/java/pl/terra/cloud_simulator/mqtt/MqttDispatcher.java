package pl.terra.cloud_simulator.mqtt;

import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.MqttSystemMessage;

public interface MqttDispatcher {
    void handleMessage(DeviceMqtt device, MqttSystemMessage message) throws SystemException;
}
