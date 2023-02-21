package pl.terra.cloud_iot.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.terra.cloud_iot.domain.DeviceService;
import pl.terra.http.api.DeviceApi;
import pl.terra.http.model.*;

import java.util.List;

@RestController
public class DeviceRestController implements DeviceApi {
    private static final Logger logger = LogManager.getLogger(DeviceRestController.class);

    private final DeviceService deviceService;

    public DeviceRestController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public ResponseEntity<DeviceStatus> get(Long userId, Long deviceId) throws Exception {
        DeviceRestController.logger.info("getting device status for user: {} and deviceId: {}", userId, deviceId);

        return ResponseEntity.ok(deviceService.getDeviceStatus(userId, deviceId));
    }

    @Override
    public ResponseEntity<List<Device>> getAll(Long userId) throws Exception {
        DeviceRestController.logger.info("getting all device for user: {}", userId);

        final List<Device> devices = deviceService.getAllForUser(userId);

        return ResponseEntity.ok(devices);
    }

    @Override
    public ResponseEntity<DeviceStatus> update(Long userId, Long deviceId, DeviceUpdate deviceUpdate) throws Exception {
        DeviceRestController.logger.info("updating device properties all device for user: {}, props: '{}'", userId, deviceUpdate);

        return ResponseEntity.ok(deviceService.updateDevice(userId, deviceId, deviceUpdate));
    }

    @Override
    public ResponseEntity<Void> delete(Long userId, Long deviceId) throws Exception {
        DeviceRestController.logger.info("deleting device {} for user: {}.", userId, deviceId);
        deviceService.delete(userId, deviceId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<DeviceStatus> setNewName(Long userId, Long deviceId, InlineObject inlineObject) throws Exception {
        DeviceRestController.logger.info("setting new name for device: {} for user: {} and new name: {}", deviceId, userId, inlineObject.getName());

        return ResponseEntity.ok(deviceService.setNewName(userId, deviceId, inlineObject.getName()));
    }

    @Override
    public ResponseEntity<List<EnvInfoDate>> getEnvInfo(Long userId, Long deviceId, Long page) throws Exception {
        DeviceRestController.logger.info("getting new name for device: {} for user: {} and page: {}", deviceId, userId, page);

        return ResponseEntity.ok(deviceService.getEnvInfoForDevice(userId, deviceId, page));
    }
}
