package pl.terra.cloud_simulator.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.common.mqtt.MqttCore;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeviceMqttDriver extends MqttCore {
    private static final Logger logger = LogManager.getLogger(DeviceMqttDriver.class);

    private final List<MqttDispatcher> dispatchers = new ArrayList<>();


    public DeviceMqttDriver(@Value("${mqtt.broker}") final String brokerUrl, @Value("${mqtt.username}") final String username,
                            @Value("${mqtt.password}") final String password) throws SystemException {
        super(brokerUrl, username, password, "device-simulator");
        DeviceMqttDriver.logger.info(String.format("'%s' class created!", this.getClass().getName()));
    }

    public void sendToBackend(final DeviceMqtt deviceMqtt, MqttSystemMessage message) throws SystemException {
        publish(deviceMqtt.getToServiceTopic(), message);
    }

    public void addDispatcher(MqttDispatcher mqttDispatcher) {
        dispatchers.add(mqttDispatcher);
    }

    @Override
    protected void messageArrived(final DeviceMqtt deviceMqtt, final MqttSystemMessage message) throws SystemException {
        if (message.getType() == MessageType.PING) {
            message.setType(MessageType.OK);
            this.sendToBackend(deviceMqtt, message);
        }
        for (final MqttDispatcher dispatcher : dispatchers) {
            dispatcher.handleMessage(deviceMqtt, message);
        }
    }

}
