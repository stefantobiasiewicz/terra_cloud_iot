package pl.terra.cloud_simulator.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.terra.common.Arguments;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.Device;
import pl.terra.common.mqtt.MqttCore;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;

@Component
public class DeviceMqttDriver extends MqttCore {
    private static final Logger logger = LogManager.getLogger(DeviceMqttDriver.class);
    public DeviceMqttDriver(@Value("${mqtt.broker}") final String brokerUrl, @Value("${mqtt.username}") final String username,
                            @Value("${mqtt.password}") final String password) throws SystemException {
        super(brokerUrl, username, password, "device-simulator");
        DeviceMqttDriver.logger.info(String.format("'%s' class created!", this.getClass().getName()));
    }

    public void sendToBackend(final Device device, MqttSystemMessage message) throws SystemException {
        publish(device.getToServiceTopic(), message);
    }

    @Override
    protected void messageArrived(final Device device, final MqttSystemMessage message) throws SystemException {
        final MqttSystemMessage response = new MqttSystemMessage();
        response.setMessageId(message.getMessageId());
        response.setType(MessageType.OK);
        response.setPayload(null);

        sendToBackend(device, response);
    }

}
