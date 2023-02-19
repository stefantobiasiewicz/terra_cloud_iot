package pl.terra.cloud_iot.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.TerraCloudIotApplication;
import pl.terra.cloud_iot.domain.CollectingService;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_simulator.TerraDeviceSimulatorApplication;
import pl.terra.cloud_simulator.controller.SimulatorApi;
import pl.terra.http.api.DeviceApi;
import pl.terra.http.api.OnboardingApi;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StatusRequestTest extends IntegrationTestBase {
    @Autowired
    SimulatorApi simulatorApi;
    @Autowired
    OnboardingApi onboardingApi;
    @Autowired
    DeviceApi deviceApi;
    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    CollectingService collectingService;

    @BeforeAll
    void prepareDevices() throws Exception {
        final Long userId = 5L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();

        // suer call
        onboardingApi.addDeviceToPoolList(userId, deviceCode);

        //device "start onboarding button"
        simulatorApi.authorizeDevice(userId);

        await().until(() -> {
            final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);
            Assertions.assertNotNull(testResult);
            return testResult.getStatus() == DeviceStatus.READY;
        });
    }

    @Test
    void checkUserRequestDeviceForStatusData() throws Exception {
        final Long userId = 5L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();
        final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);

        Assertions.assertNotNull(testResult);
        final Long deviceId = testResult.getId();


        ResponseEntity<pl.terra.http.model.DeviceStatus> statusResponse = deviceApi.get(userId, deviceId);

        pl.terra.http.model.DeviceStatus status = statusResponse.getBody();

        Assertions.assertNotNull(status);
        Assertions.assertEquals(deviceId, status.getDevice().getId());

        ResponseEntity<pl.terra.http.model.DeviceStatus> statusResponse2 = deviceApi.get(userId, deviceId);

        pl.terra.http.model.DeviceStatus status2 = statusResponse2.getBody();

        Assertions.assertNotNull(status2);
        Assertions.assertEquals(deviceId, status2.getDevice().getId());
    }

}
