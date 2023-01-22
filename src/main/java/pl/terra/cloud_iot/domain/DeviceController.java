package pl.terra.cloud_iot.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.mqtt.MqttDispatcher;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.device.model.MqttSystemMessage;

@Component
public class DeviceController implements MqttDispatcher {
    private static final Logger logger = LogManager.getLogger(DeviceController.class);
    private final ServiceMqttDriver serviceMqttDriver;
    private final DeviceService deviceService;
    public DeviceController(final ServiceMqttDriver serviceMqttDriver, DeviceService deviceService) {
        this.serviceMqttDriver = serviceMqttDriver;
        this.deviceService = deviceService;
        this.serviceMqttDriver.addDispatcher(this);
    }

    @Override
    public void handleMessage(DeviceEntity device, MqttSystemMessage message) {
        DeviceController.logger.debug(String.format("get message for device: '%s' and message: '%s'", device, message));
        switch (message.getType()) {
            case AUTHORIZE:
                deviceService.setStatusReady(device);
                break;
            case OK:
            case PING:
            case ERROR:
            case ENV_INFO:
            case LIGHT_CMD:

                break;
            default:
                break;
        }
    }
}
