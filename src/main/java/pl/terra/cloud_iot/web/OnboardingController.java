package pl.terra.cloud_iot.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.terra.cloud_iot.domain.DeviceService;
import pl.terra.http.api.OnboardingApi;
import pl.terra.http.model.Connection;

@RestController
public class OnboardingController implements OnboardingApi {
    private static final Logger logger = LogManager.getLogger(OnboardingController.class);

    final DeviceService deviceService;

    public OnboardingController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public ResponseEntity<Void> addDeviceToPoolList(final Long userId, final String deviceCode) throws Exception {
        OnboardingController.logger.debug(String.format("Adding device to pool list with userId: %d and device code: '%s'", userId, deviceCode));

        deviceService.addToPool(userId, deviceCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Connection> getConnection(String deviceCode) throws Exception {
        OnboardingController.logger.debug(String.format("getting connection device with device code: '%s'", deviceCode));

        final Connection connection = deviceService.getConnection(deviceCode);

        return ResponseEntity.ok(connection);
    }
}
