package pl.terra.cloud_iot.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.cloud_iot.utils.DeviceEntityUtils;
import pl.terra.common.exception.NotFoundException;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.http.model.Connection;
import pl.terra.http.model.Device;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceService {
    private static final Logger logger = LogManager.getLogger(DeviceService.class);
    private final ServiceMqttDriver serviceMqttDriver;
    private final DeviceRepository deviceRepository;

    public DeviceService(ServiceMqttDriver serviceMqttDriver, DeviceRepository deviceRepository) {
        this.serviceMqttDriver = serviceMqttDriver;
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllForUser(Long userId) {
        return deviceRepository.findAllByUserId(userId).stream()
                .map(DeviceEntityUtils::mapToDevice).collect(Collectors.toList());
    }
}
