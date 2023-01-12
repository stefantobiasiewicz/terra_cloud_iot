package pl.terra.cloud_iot.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;

@Component
public class DeviceService {
    private static final Logger logger = LogManager.getLogger(DeviceService.class);
    private final ServiceMqttDriver serviceMqttDriver;

    public DeviceService(ServiceMqttDriver serviceMqttDriver) {
        this.serviceMqttDriver = serviceMqttDriver;
    }


}
