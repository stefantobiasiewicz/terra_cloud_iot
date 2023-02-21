package pl.terra.cloud_iot.domain;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.exceptions.AlreadyExistException;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.common.exception.NotFoundException;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.http.model.Connection;

import java.time.LocalDate;
import java.util.List;

@Component
public class OnboardingService {
    private static final Logger logger = LogManager.getLogger(OnboardingService.class);

    private final ServiceMqttDriver serviceMqttDriver;
    private final DeviceRepository deviceRepository;

    public OnboardingService(ServiceMqttDriver serviceMqttDriver, DeviceRepository deviceRepository) throws SystemException {
        this.serviceMqttDriver = serviceMqttDriver;
        this.deviceRepository = deviceRepository;

        final List<DeviceEntity> deviceEntityList = deviceRepository.findAllNotDeleted();

        for (final DeviceEntity entity : deviceEntityList) {
            final DeviceMqtt deviceMqtt = new DeviceMqtt();
            deviceMqtt.setToServiceTopic(entity.getToServiceTopic());
            deviceMqtt.setToDeviceTopic(entity.getToDeviceTopic());
            deviceMqtt.setId(entity.getId());
            serviceMqttDriver.registerDevice(deviceMqtt);
        }
    }

    private String getToDeviceTopic(final String deviceCode) {
        return String.format("/tai/device/%s", deviceCode);
    }

    private String getToServiceTopic(final String deviceCode) {
        return String.format("/tai/service/%s", deviceCode);
    }

    public void addToPool(final Long userId, final String deviceCode) throws SystemException {
        OnboardingService.logger.debug(String.format("Adding device to pool list with userId: %d and device code: '%s'", userId, deviceCode));

        if (deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).isPresent()) {
            throw new AlreadyExistException(String.format("device with code: '%s' already exist", deviceCode));
        }

        final DeviceEntity entity = new DeviceEntity();
        entity.setUserId(userId);
        entity.setFactoryCode(deviceCode);
        entity.setCreatedAt(LocalDate.now());
        entity.setStatus(DeviceStatus.PENDING);
        entity.setToServiceTopic(getToServiceTopic(deviceCode));
        entity.setToDeviceTopic(getToDeviceTopic(deviceCode));
        entity.setName("New device!");
        deviceRepository.save(entity);

        final DeviceMqtt deviceMqtt = new DeviceMqtt();
        deviceMqtt.setToServiceTopic(entity.getToServiceTopic());
        deviceMqtt.setToDeviceTopic(entity.getToDeviceTopic());
        deviceMqtt.setId(entity.getId());
        serviceMqttDriver.registerDevice(deviceMqtt);
    }

    public Connection getConnection(final String deviceCode) throws NotFoundException {
        OnboardingService.logger.debug(String.format("getting device connection for device code: '%s'", deviceCode));

        final DeviceEntity device = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);
        if (device == null) {
            throw new NotFoundException(String.format("can't find device with device code: %s", deviceCode));
        }

        final Connection connection = new Connection();
        connection.toDeviceTopic(device.getToDeviceTopic());
        connection.toServiceTopic(device.getToServiceTopic());

        return connection;
    }

    public void setStatusReady(final DeviceEntity device) {
        OnboardingService.logger.debug(String.format("Authorizing device: '%s'", device));

        device.setStatus(DeviceStatus.READY);
        deviceRepository.save(device);
    }
}
