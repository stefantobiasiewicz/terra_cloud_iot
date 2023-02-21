package pl.terra.cloud_iot.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.TerraCloudIotApplication;
import pl.terra.cloud_iot.domain.CollectingService;
import pl.terra.cloud_iot.domain.DeviceService;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_simulator.TerraDeviceSimulatorApplication;
import pl.terra.cloud_simulator.controller.SimulatorApi;
import pl.terra.common.exception.NotFoundException;
import pl.terra.http.api.OnboardingApi;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class GetEnvInfoTest extends IntegrationTestBase {
    @Autowired
    SimulatorApi simulatorApi;
    @Autowired
    OnboardingApi onboardingApi;
    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceService deviceService;
    @Autowired
    CollectingService collectingService;

    @BeforeAll
    void prepareDevices() throws Exception {
        final Long userId = 1L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();

        // suer call
        Assertions.assertEquals(HttpStatus.NO_CONTENT, onboardingApi.addDeviceToPoolList(userId, deviceCode).getStatusCode());

        //device "start onboarding button"
        Assertions.assertEquals(HttpStatus.OK, simulatorApi.authorizeDevice(userId).getStatusCode());

        await().until(() -> {
            final DeviceEntity testResult = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);
            Assertions.assertNotNull(testResult);
            return testResult.getStatus() == DeviceStatus.READY;
        });
    }

    @Test
    void checkGetEnvInfo() throws NotFoundException {
        final Long userId = 1L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();
        final DeviceEntity device = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);

        await().until(() -> collectingService.getEnvInfo(device).size() == 10);


        var list =  deviceService.getEnvInfoForDevice(userId, device.getId(), 0L);

        Assertions.assertTrue(list.size() >= 10);
    }

}
