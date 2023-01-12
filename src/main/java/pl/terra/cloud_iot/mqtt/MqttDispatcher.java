package pl.terra.cloud_iot.mqtt;

import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.device.model.MqttSystemMessage;

public interface MqttDispatcher {
    void handleMessage(DeviceEntity device, MqttSystemMessage message);
}
