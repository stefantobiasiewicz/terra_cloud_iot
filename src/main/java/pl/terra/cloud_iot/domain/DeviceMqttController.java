package pl.terra.cloud_iot.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.mqtt.MqttDispatcher;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.common.exception.SystemException;
import pl.terra.device.model.MqttSystemMessage;

@Component
public class DeviceMqttController implements MqttDispatcher {
    private static final Logger logger = LogManager.getLogger(DeviceMqttController.class);
    private final OnboardingService onboardingService;
    private final CollectingService collectingService;

    public DeviceMqttController(final ServiceMqttDriver serviceMqttDriver, OnboardingService onboardingService, CollectingService collectingService) {
        this.onboardingService = onboardingService;
        this.collectingService = collectingService;
        serviceMqttDriver.addDispatcher(this);
    }

    @Override
    public void handleMessage(DeviceEntity device, MqttSystemMessage message) throws SystemException {
        //DeviceMqttController.logger.debug(String.format("get message for device: '%s' and message: '%s'", device, message));
        DeviceMqttController.logger.debug(String.format("get message for device: '%s' and message type: '%s'", device, message.getType()));
        if(device.getStatus() != DeviceStatus.READY) {
            switch (message.getType()) {
                case AUTHORIZE:
                    onboardingService.setStatusReady(device);
                    return;
                default:
                    return;
            }
        }
        switch (message.getType()) {
            case AUTHORIZE:
                onboardingService.setStatusReady(device);
                break;
            case OK:
            case PING:
            case ERROR:
                break;
            case ENV_INFO:
                collectingService.collect(device, message);
            case LIGHT_CMD:

                break;
            default:
                break;
        }
    }
}
