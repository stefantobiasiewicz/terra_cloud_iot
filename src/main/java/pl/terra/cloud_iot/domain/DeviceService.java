package pl.terra.cloud_iot.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.cloud_iot.utils.DeviceEntityUtils;
import pl.terra.common.exception.NotFoundException;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;
import pl.terra.device.model.StatusResp;
import pl.terra.http.model.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.Date;
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
                .map(DeviceEntityUtils::mapToDevice).collect(Collectors.toList());
    }

    public DeviceStatus getDeviceStatus(Long userId, Long deviceId) throws SystemException, JsonProcessingException {
        final List<DeviceEntity> devices = deviceRepository.findAllByUserIdAndId(userId, deviceId);

        if(devices.size() < 1) {
            DeviceService.logger.error("can't find device for userId: {} and deviceId: {}", userId, deviceId);
            throw new NotFoundException(String.format("can't find device for userId: %s and deviceId: %s", userId, deviceId));
        }

        final MqttSystemMessage request = new MqttSystemMessage();
        request.setType(MessageType.STATUS_REQ);
        request.setMessageId(new Random().nextLong());

        DeviceMqtt device = serviceMqttDriver.getDeviceById(deviceId);

        DeviceService.logger.info("requesting device: '{}' by: '{}'", device, request);
        final MqttSystemMessage response = serviceMqttDriver.exchange(device, request, 10000L);

        StatusResp payload = ServiceMqttDriver.getPayloadClass(response, StatusResp.class);

        final DeviceStatus result = new DeviceStatus();
        result.setDevice(DeviceEntityUtils.mapToDevice(devices.get(0)));
        result.setDoors(payload.getDoors());
        result.setFan(payload.getFan());

        final DeviceStatusHeater heater = new DeviceStatusHeater();
        heater.setOnOff(payload.getHeater().getOnOff());
        heater.setTemp(payload.getHeater().getSetTemp());
        result.setHeater(heater);
        result.setHumidifier(payload.getHumidifier());

        final EnvInfo envInfo = new EnvInfo();
        envInfo.setHumidity(payload.getEnvInfo().getHumidity());
        envInfo.setPressure(payload.getEnvInfo().getPressure());
        envInfo.setTemperature(payload.getEnvInfo().getTemperature());
        result.setEnvInfo(envInfo);

        return result;
    }
}
