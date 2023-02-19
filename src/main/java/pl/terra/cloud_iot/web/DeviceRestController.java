package pl.terra.cloud_iot.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.terra.cloud_iot.domain.DeviceService;
import pl.terra.http.api.DeviceApi;
import pl.terra.http.model.Device;
import pl.terra.http.model.DeviceStatus;

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
}
