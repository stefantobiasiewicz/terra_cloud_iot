package pl.terra.cloud_iot.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.EnvInfoEntity;
import pl.terra.cloud_iot.jpa.repository.EnvInfoRepository;
import pl.terra.common.Arguments;
import pl.terra.common.exception.SystemException;
import pl.terra.device.model.EnvInfo;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CollectingService {
    private static final Logger logger = LogManager.getLogger(CollectingService.class);

    private final EnvInfoRepository envInfoRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public CollectingService(EnvInfoRepository envInfoRepository) {
        this.envInfoRepository = envInfoRepository;
    }

    private void checkMessage(final DeviceEntity device, final MqttSystemMessage message) throws SystemException {
        if (message.getType() != MessageType.ENV_INFO) {
            final String msg = String.format("message type is not ENV_INFO for message: '%s' and device: '%s", message, device);
            CollectingService.logger.error(msg);
            throw new SystemException(msg);
        }

        if (message.getPayload() == null) {
            final String msg = String.format("env info payload is null for message: '%s' and device: '%s", message, device);
            CollectingService.logger.error(msg);
            throw new SystemException(msg);
        }
    }

    public void collect(final DeviceEntity device, final MqttSystemMessage message) throws SystemException {
        Arguments.isNull(device, "device");
        Arguments.isNull(message, "message");
        checkMessage(device, message);

        EnvInfo envInfo = null;
        try {
            envInfo = mapper.readValue(mapper.writeValueAsString(message.getPayload()), EnvInfo.class);
        } catch (JsonProcessingException e) {
            final String msg = String.format("can't get env info payload from message: '%s' and device: '%s", message, device);
            CollectingService.logger.error(msg);
            throw new SystemException(msg);
        }

        final EnvInfoEntity entity = new EnvInfoEntity();
        entity.setDevice(device);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTemp(envInfo.getTemperature());
        entity.setPres(envInfo.getPressure());
        entity.setHum(entity.getHum());

        envInfoRepository.save(entity);
    }


    public List<EnvInfoEntity> getEnvInfo(final Long n) {
        return envInfoRepository.findAll(Pageable.ofSize(n.intValue())).toList();
    }
}
