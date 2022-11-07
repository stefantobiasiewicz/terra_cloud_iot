package pl.terra.cloud_iot.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.common.Arguments;
import pl.terra.cloud_iot.common.exception.SystemException;
import pl.terra.device.model.MqttSystemMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MqttCore implements MqttCallback {
    private final static Logger logger = LogManager.getLogger(MqttCore.class);

    private final int qos = 1;

    private final MqttClient client;

    final ObjectMapper mapper = new ObjectMapper();
    private List<Map.Entry<String, Long>> ackList = new ArrayList<>();


    public MqttCore(@Value("${mqtt.broker}") final String brokerUrl, @Value("${mqtt.username}")final String username,
                    @Value("${mqtt.password}") final String password, @Value("${mqtt.clientId}") final String clientId)
            throws SystemException {
        Arguments.isNullOrEmpty(brokerUrl, "brokerUrl");
        Arguments.isNullOrEmpty(username, "username");
        Arguments.isNullOrEmpty(password, "password");
        Arguments.isNullOrEmpty(clientId, "clientId");

        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            client.connect(options);
            client.setCallback(this);
        } catch (final MqttException e) {
            final String message = String.format("can't create mqtt connection.");
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }
    }


    public void publish(final String topic, final MqttSystemMessage mqttSystemMessage) throws SystemException {
        Arguments.isNull(mqttSystemMessage, "mqttSystemMessage");
        Arguments.isNullOrEmpty(topic, "topic");

        final String payload;
        try {
            payload = mapper.writeValueAsString(mqttSystemMessage);
        } catch (JsonProcessingException e) {
            final String message = String.format("can't parse json to string: '%s'.", mqttSystemMessage);
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }

        MqttMessage mqttMessage = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        mqttMessage.setQos(qos);
        try {
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            final String message = String.format("can't publish message: '%s' on topic: '%s'.", topic, payload);
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }
    }

    public boolean publishBlock(final String topic, final MqttSystemMessage mqttSystemMessage, final Long timeout)
            throws SystemException, JsonProcessingException {
        Arguments.isNullOrEmpty(topic, "topic");
        Arguments.isNull(mqttSystemMessage, "mqttSystemMessage");
        Arguments.isNull(timeout, "timeout");

        publish(topic, mqttSystemMessage);

        ackList.add(new ImmutablePair<>(topic, mqttSystemMessage.getMessageId()));

        // avait for response

        return true;
    }

    @Override
    public void connectionLost(final Throwable throwable) {

    }

    @Override
    public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {


    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
