package pl.terra.common.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import pl.terra.common.Arguments;
import pl.terra.common.exception.MqttTimeoutException;
import pl.terra.common.exception.NotFoundException;
import pl.terra.common.exception.SystemException;
import pl.terra.device.model.MqttSystemMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class MqttCore implements MqttCallback {
    private final static Logger logger = LogManager.getLogger(MqttCore.class);

    public final int qos = 0;

    private final MqttAsyncClient client;
    private final List<DeviceMqtt> registeredDeviceMqtts = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, MqttSystemMessage> messageMap = new HashMap<>();


    public MqttCore(final String brokerUrl, final String username, final String password, final String clientId)
            throws SystemException {
        Arguments.isNullOrEmpty(brokerUrl, "brokerUrl");
        Arguments.isNullOrEmpty(username, "username");
        Arguments.isNullOrEmpty(password, "password");
        Arguments.isNullOrEmpty(clientId, "clientId");

        try {
            client = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            IMqttToken token = client.connect(options);
            token.waitForCompletion();
            client.setCallback(this);
        } catch (final MqttException e) {
            final String message = "can't create mqtt connection.";
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }

        MqttCore.logger.info(String.format("'%s' class created!", this.getClass().getName()));
    }


    private void checkDevice(final DeviceMqtt deviceMqtt) throws SystemException {
        Arguments.isNull(deviceMqtt, "device");
        if (!registeredDeviceMqtts.contains(deviceMqtt)) {
            final String message = String.format("can't find device: '%s' in added device list.", deviceMqtt);
            MqttCore.logger.error(message);
            throw new SystemException(message);
        }
    }

    public void registerDevice(final DeviceMqtt deviceMqtt) throws SystemException {
        Arguments.isNull(deviceMqtt, "device");

        try {
            client.subscribe(deviceMqtt.getToServiceTopic(), qos);
        } catch (MqttException e) {
            final String message = String.format("can't subscribe topic: '%s'.", deviceMqtt.getToServiceTopic());
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }

        registeredDeviceMqtts.add(deviceMqtt);
    }

    public void remove(final DeviceMqtt deviceMqtt) throws SystemException {
        Arguments.isNull(deviceMqtt, "device");

        try {
            client.unsubscribe(deviceMqtt.getToServiceTopic());
        } catch (MqttException e) {
            final String message = String.format("can't unsubscribe topic: '%s'.", deviceMqtt.getToServiceTopic());
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }

        registeredDeviceMqtts.remove(deviceMqtt);
    }

    public void registerService(final DeviceMqtt deviceMqtt) throws SystemException {
        Arguments.isNull(deviceMqtt, "device");

        try {
            client.subscribe(deviceMqtt.getToDeviceTopic(), qos);
        } catch (MqttException e) {
            final String message = String.format("can't subscribe topic: '%s'.", deviceMqtt.getToServiceTopic());
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }

        registeredDeviceMqtts.add(deviceMqtt);
    }

    public void removeService(final DeviceMqtt deviceMqtt) throws SystemException {
        Arguments.isNull(deviceMqtt, "device");

        try {
            client.unsubscribe(deviceMqtt.getToDeviceTopic());
        } catch (MqttException e) {
            final String message = String.format("can't unsubscribe topic: '%s'.", deviceMqtt.getToServiceTopic());
            MqttCore.logger.error(message, e);
            throw new SystemException(message);
        }

        registeredDeviceMqtts.remove(deviceMqtt);
    }

    protected void publish(final String topic, final MqttSystemMessage mqttSystemMessage) throws SystemException {
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

    public MqttSystemMessage exchange(final DeviceMqtt deviceMqtt, final MqttSystemMessage mqttSystemMessage,
                                      final Long timeout) throws SystemException {
        Arguments.isNull(deviceMqtt, "device");
        Arguments.isNull(mqttSystemMessage, "mqttSystemMessage");
        Arguments.isNull(timeout, "timeout");

        checkDevice(deviceMqtt);

        messageMap.put(mqttSystemMessage.getMessageId(), null);

        publish(deviceMqtt.getToDeviceTopic(), mqttSystemMessage);


        final long start = System.currentTimeMillis();
        while (messageMap.get(mqttSystemMessage.getMessageId()) == null) {
            final long duration = System.currentTimeMillis() - start;
            if (duration >= timeout) {
                final String message = String.format("timeout waiting for ack message, messageId: %d, timeout: %d",
                        mqttSystemMessage.getMessageId(), duration);
                messageMap.remove(mqttSystemMessage.getMessageId());

                MqttCore.logger.error(message);
                throw new MqttTimeoutException(message);
            }
        }

        return messageMap.remove(mqttSystemMessage.getMessageId());
    }

    @Override
    public void connectionLost(final Throwable throwable) {

    }

    @Override
    public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {
        Arguments.isNullOrEmpty(topic, "topic");
        Arguments.isNull(mqttMessage, "mqttMessage");
        MqttCore.logger.debug(String.format("arrived message: '%s' on topic: '%s'", mqttMessage, topic));

        MqttSystemMessage message = null;
        try {
            message = mapper.readValue(new String(mqttMessage.getPayload()), MqttSystemMessage.class);
        } catch (JsonProcessingException e) {
            final String errorMessage =
                    String.format("can't parse payload form mqtt: '%s' on topic: '%s' to system message.",
                            new String(mqttMessage.getPayload()), topic);
            MqttCore.logger.error(errorMessage, e);
            throw new SystemException(errorMessage);
        }

        if (messageMap.containsKey(message.getMessageId())) {
            messageMap.put(message.getMessageId(), message);
            return;
        }

        final DeviceMqtt deviceMqtt = registeredDeviceMqtts.stream()
                .filter(e -> e.getToDeviceTopic().equals(topic) || e.getToServiceTopic().equals(topic))
                .findFirst()
                .orElse(null);
        if (deviceMqtt == null) {
            throw new SystemException(String.format("can't find device in registered devices for topic: '%s'.", topic));
        }

        messageArrived(deviceMqtt, message);
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            iMqttDeliveryToken.getMessage();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void messageArrived(DeviceMqtt deviceMqtt, MqttSystemMessage message) throws SystemException;

    public DeviceMqtt getDeviceById(Long deviceId) throws NotFoundException {
        return registeredDeviceMqtts.stream()
                .filter(deviceMqtt -> deviceMqtt.getId() == deviceId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("can't find deivce in mqtt core with id: %d", deviceId)));
    }

    public static <T> T getPayloadClass(MqttSystemMessage message, Class<T> clazz) throws SystemException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(mapper.writeValueAsString(message.getPayload()), clazz);
        } catch (JsonProcessingException e) {
            MqttCore.logger.error("can't parse message: '{}' on class: '{}'", message, clazz);
            throw new SystemException(String.format("can't parse message: '%s'", message));
        }
    }
}
