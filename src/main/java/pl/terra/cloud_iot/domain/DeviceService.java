package pl.terra.cloud_iot.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.terra.cloud_iot.exceptions.ConflictException;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.cloud_iot.utils.MapperUtils;
import pl.terra.common.exception.NotFoundException;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;
import pl.terra.device.model.StatusResp;
import pl.terra.http.model.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DeviceService {
    private static final Logger logger = LogManager.getLogger(DeviceService.class);
    private final ServiceMqttDriver serviceMqttDriver;
    private final DeviceRepository deviceRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public DeviceService(ServiceMqttDriver serviceMqttDriver, DeviceRepository deviceRepository) {
        this.serviceMqttDriver = serviceMqttDriver;
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllForUser(Long userId) {
        return deviceRepository.findAllByUserId(userId).stream()
                .map(MapperUtils::mapToDevice).collect(Collectors.toList());
    }

    public DeviceStatus getDeviceStatus(Long userId, Long deviceId) throws SystemException, JsonProcessingException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndActive(userId, deviceId);

        if (devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        if(devices.get(0).getStatus() != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.READY) {
            throw new ConflictException("device is not in state READY");
        }

        final MqttSystemMessage request = new MqttSystemMessage();
        request.setType(MessageType.STATUS_REQ);
        request.setMessageId(new Random().nextLong());

        DeviceMqtt device = serviceMqttDriver.getDeviceById(deviceId);

        DeviceService.logger.info("requesting device: '{}' by: '{}'", device, request);
        final MqttSystemMessage response = serviceMqttDriver.exchange(device, request, 10000L);

        StatusResp payload = ServiceMqttDriver.getPayloadClass(response, StatusResp.class);

        return MapperUtils.mapToDeviceStatus(payload, devices.get(0));
    }

    public DeviceStatus updateDevice(final Long userId, final Long deviceId, final DeviceUpdate deviceUpdate) throws SystemException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndActive(userId, deviceId);

        if (devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        if(devices.get(0).getStatus() != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.READY) {
            throw new ConflictException("device is not in state READY");
        }


        final MqttSystemMessage request = new MqttSystemMessage();
        request.setType(MessageType.UPDATE_REQ);
        request.setMessageId(new Random().nextLong());
        request.setPayload(MapperUtils.mapToUpdateRequest(deviceUpdate));

        DeviceMqtt device = serviceMqttDriver.getDeviceById(deviceId);

        DeviceService.logger.info("requesting device: '{}' by: '{}'", device, request);
        final MqttSystemMessage response = serviceMqttDriver.exchange(device, request, 10000L);

        StatusResp payload = ServiceMqttDriver.getPayloadClass(response, StatusResp.class);

        return MapperUtils.mapToDeviceStatus(payload, devices.get(0));
    }

    @Transactional
    public void delete(final Long userId, final Long deviceId) throws SystemException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndActive(userId, deviceId);

        if (devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        if(devices.get(0).getStatus() != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.READY) {
            throw new ConflictException("device is not in state READY");
        }


        final DeviceEntity deviceEntity = devices.get(0);

        deviceEntity.setStatus(pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED);
        deviceRepository.save(deviceEntity);

        final MqttSystemMessage request = new MqttSystemMessage();
        request.setType(MessageType.DELETE_REQ);
        request.setMessageId(new Random().nextLong());

        DeviceMqtt device = serviceMqttDriver.getDeviceById(deviceId);

        DeviceService.logger.info("deleting device: '{}' by: '{}'", device, request);
        final MqttSystemMessage response = serviceMqttDriver.exchange(device, request, 10000L);

        if(response.getType() == MessageType.OK) {
            serviceMqttDriver.remove(device);
            return;
        }

        throw new SystemException("can't remove device");
    }
}
