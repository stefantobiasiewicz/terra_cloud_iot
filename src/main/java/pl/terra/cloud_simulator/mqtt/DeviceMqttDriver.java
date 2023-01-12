package pl.terra.cloud_simulator.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.MqttCore;

@Component
public class DeviceMqttDriver extends MqttCore {
    private static final Logger logger = LogManager.getLogger(DeviceMqttDriver.class);
    public DeviceMqttDriver(@Value("${mqtt.broker}") final String brokerUrl, @Value("${mqtt.username}") final String username,
                            @Value("${mqtt.password}") final String password, @Value("${mqtt.clientId}") final String clientId) throws SystemException {
        super(brokerUrl, username, password, clientId);
        DeviceMqttDriver.logger.info(String.format("'%s' class created!", this.getClass().getName()));
    }
}
