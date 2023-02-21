package pl.terra.cloud_iot.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.common.mqtt.MqttCore;
import pl.terra.device.model.MqttSystemMessage;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServiceMqttDriver extends MqttCore {
    private static final Logger logger = LogManager.getLogger(ServiceMqttDriver.class);

    private final List<MqttDispatcher> dispatchers = new ArrayList<>();

    @Autowired
    private DeviceRepository deviceRepository;

    public ServiceMqttDriver(@Value("${mqtt.broker}") final String brokerUrl, @Value("${mqtt.username}") final String username,
                             @Value("${mqtt.password}") final String password, @Value("${mqtt.clientId}") final String clientId) throws SystemException {
        super(brokerUrl, username, password, clientId);
        ServiceMqttDriver.logger.info(String.format("'%s' class created!", this.getClass().getName()));
    }

    public void addDispatcher(MqttDispatcher mqttDispatcher) {
        dispatchers.add(mqttDispatcher);
    }

    @Override
    protected void messageArrived(DeviceMqtt deviceMqtt, MqttSystemMessage message) throws SystemException {
        DeviceEntity device = deviceRepository.findByDeviceMqttAndNotDeleted(deviceMqtt.getToDeviceTopic(), deviceMqtt.getToServiceTopic()).orElse(null);
        if (device == null) {
            final String errorMessage = String.format("can't find device with topics: '%s' '%s'",
                    deviceMqtt.getToDeviceTopic(), deviceMqtt.getToServiceTopic());
            ServiceMqttDriver.logger.error(errorMessage);
            throw new SystemException(errorMessage);
        }
        for (final MqttDispatcher dispatcher : dispatchers) {
            dispatcher.handleMessage(device, message);
        }
    }
}
