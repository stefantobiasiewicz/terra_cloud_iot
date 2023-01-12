package pl.terra.cloud_iot.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.Device;
import pl.terra.common.mqtt.MqttCore;
import pl.terra.device.model.MqttSystemMessage;

@Component
public class ServiceMqttDriver extends MqttCore {
    private static final Logger logger = LogManager.getLogger(ServiceMqttDriver.class);
    public ServiceMqttDriver(@Value("${mqtt.broker}") final String brokerUrl, @Value("${mqtt.username}") final String username,
                             @Value("${mqtt.password}") final String password, @Value("${mqtt.clientId}") final String clientId) throws SystemException {
        super(brokerUrl, username, password, clientId);
        ServiceMqttDriver.logger.info(String.format("'%s' class created!", this.getClass().getName()));
    }

    @Override
    protected void messageArrived(Device device, MqttSystemMessage message) throws SystemException {

    }
}
