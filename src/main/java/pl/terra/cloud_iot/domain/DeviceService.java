package pl.terra.cloud_iot.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.bind.v2.schemagen.episode.Package;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.terra.cloud_iot.exceptions.ConflictException;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.EnvInfoEntity;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_iot.jpa.repository.EnvInfoRepository;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.cloud_iot.utils.MapperUtils;
import pl.terra.common.exception.NotFoundException;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;
import pl.terra.device.model.StatusResp;
import pl.terra.http.model.*;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DeviceService {
    private static final Logger logger = LogManager.getLogger(DeviceService.class);
    private final ServiceMqttDriver serviceMqttDriver;
    private final DeviceRepository deviceRepository;
    private final EnvInfoRepository envInfoRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public DeviceService(ServiceMqttDriver serviceMqttDriver, DeviceRepository deviceRepository, EnvInfoRepository envInfoRepository) {
        this.serviceMqttDriver = serviceMqttDriver;
        this.deviceRepository = deviceRepository;
        this.envInfoRepository = envInfoRepository;
    }

    public List<Device> getAllForUser(Long userId) {
        return deviceRepository.findAllByUserIdAndNotDeleted(userId).stream()
                .map(MapperUtils::mapToDevice).collect(Collectors.toList());
    }

    public DeviceStatus getDeviceStatus(Long userId, Long deviceId) throws SystemException, JsonProcessingException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndNotDeleted(userId, deviceId);

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
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndNotDeleted(userId, deviceId);

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
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndNotDeleted(userId, deviceId);

        if (devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        final DeviceEntity deviceEntity = devices.get(0);

        if(deviceEntity.getStatus() == pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.READY) {
            final MqttSystemMessage request = new MqttSystemMessage();
            request.setType(MessageType.DELETE_REQ);
            request.setMessageId(new Random().nextLong());

            DeviceMqtt device = serviceMqttDriver.getDeviceById(deviceId);

            DeviceService.logger.info("deleting device: '{}' by: '{}'", device, request);
            final MqttSystemMessage response = serviceMqttDriver.exchange(device, request, 10000L);

            if(response.getType() != MessageType.OK) {
                throw new SystemException("can't remove device");
            }
            serviceMqttDriver.remove(device);
        }

        deviceEntity.setStatus(pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED);
        deviceRepository.save(deviceEntity);
    }

    public DeviceStatus setNewName(final Long userId, final Long deviceId, final String newName) throws SystemException, JsonProcessingException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndNotDeleted(userId, deviceId);

        if (devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        DeviceService.logger.info("setNewName device: {} for user: {} and new name: {}", deviceId, userId, newName);


        final DeviceEntity deviceEntity = devices.get(0);

        deviceEntity.setName(newName);

        deviceRepository.save(deviceEntity);

        if(deviceEntity.getStatus() == pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.READY) {
            return getDeviceStatus(userId,deviceId);
        }
        return null;
    }

    public List<EnvInfoDate> getEnvInfoForDevice(final Long userId, final Long deviceId, final Long page) throws NotFoundException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndIdAndNotDeleted(userId, deviceId);

        if (devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        if(devices.get(0).getStatus() != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.READY) {
            throw new ConflictException("device is not in state READY");
        }

        final DeviceEntity deviceEntity = devices.get(0);

        final List<EnvInfoEntity> entities = envInfoRepository.findAllByDevice(deviceEntity, PageRequest.of(page.intValue(), 30, Sort.by("id").descending()));

        return entities.stream().map(envInfoEntity -> {
            final EnvInfoDate envInfo = new EnvInfoDate();
            envInfo.setHumidity(envInfoEntity.getHum());
            envInfo.setPressure(envInfoEntity.getPres());
            envInfo.setTemperature(envInfoEntity.getTemp());
            envInfo.setCreatedAt(envInfoEntity.getCreatedAt().atOffset(ZoneOffset.UTC));
            return envInfo;
        }).collect(Collectors.toList());
    }
}
