package pl.terra.cloud_iot.integration;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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

import java.time.Duration;
import java.util.stream.Collectors;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DeleteRequestTest extends IntegrationTestBase {
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

    }

    @Test
    @Order(100)
    void checkDelete() throws Exception {
        final Long userId = 24L;
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

        final DeviceEntity testResult = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);

        Assertions.assertNotNull(testResult);
        final Long deviceId = testResult.getId();

        await().until(() -> collectingService.getEnvInfo(testResult).size() == 3);

        ResponseEntity<Void> statusResponse = deviceApi.delete(userId, deviceId);
        Assertions.assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        int size = collectingService.getEnvInfo(testResult).size();
        await()
                .during(Duration.ofSeconds(3)) // during this period, the condition should be maintained true
                .atMost(Duration.ofSeconds(11)) // timeout
                .until (() ->
                        size == collectingService.getEnvInfo(testResult).size()          // the maintained condition
                );

        boolean authorize = simulatorApi.getAllDeviceIds().getBody()
                .stream()
                .filter(devicePair -> devicePair.getCode().equals(deviceCode))
                .collect(Collectors.toList())
                .get(0)
                .isAuthorized();
        Assertions.assertFalse(authorize);
    }

    @Test
    @Order(200)
    void checkAddingAndDelete() throws Exception {
        final Long userId = 24L;
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

        final DeviceEntity testResult = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);

        Assertions.assertNotNull(testResult);
        final Long deviceId = testResult.getId();

        await().until(() -> collectingService.getEnvInfo(testResult).size() == 3);

        ResponseEntity<Void> statusResponse = deviceApi.delete(userId, deviceId);
        Assertions.assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        int size = collectingService.getEnvInfo(testResult).size();
        await()
                .during(Duration.ofSeconds(3)) // during this period, the condition should be maintained true
                .atMost(Duration.ofSeconds(11)) // timeout
                .until (() ->
                        size == collectingService.getEnvInfo(testResult).size()          // the maintained condition
                );

        boolean authorize = simulatorApi.getAllDeviceIds().getBody()
                .stream()
                .filter(devicePair -> devicePair.getCode().equals(deviceCode))
                .collect(Collectors.toList())
                .get(0)
                .isAuthorized();
        Assertions.assertFalse(authorize);


        // ***** second time

        // suer call
        Assertions.assertEquals(HttpStatus.NO_CONTENT, onboardingApi.addDeviceToPoolList(userId, deviceCode).getStatusCode());

        //device "start onboarding button"
        Assertions.assertEquals(HttpStatus.OK, simulatorApi.authorizeDevice(userId).getStatusCode());

        await().until(() -> {
            final DeviceEntity testResult2 = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);
            Assertions.assertNotNull(testResult2);
            return testResult2.getStatus() == DeviceStatus.READY;
        });

        final DeviceEntity testResult2 = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);

        Assertions.assertNotNull(testResult2);
        final Long deviceId2 = testResult2.getId();

        await().until(() -> collectingService.getEnvInfo(testResult2).size() == 3);

        ResponseEntity<Void> statusResponse2 = deviceApi.delete(userId, deviceId2);
        Assertions.assertEquals(HttpStatus.OK, statusResponse2.getStatusCode());

        int size2 = collectingService.getEnvInfo(testResult2).size();
        await()
                .during(Duration.ofSeconds(3)) // during this period, the condition should be maintained true
                .atMost(Duration.ofSeconds(11)) // timeout
                .until (() ->
                        size2 == collectingService.getEnvInfo(testResult2).size()          // the maintained condition
                );

        boolean authorize2 = simulatorApi.getAllDeviceIds().getBody()
                .stream()
                .filter(devicePair -> devicePair.getCode().equals(deviceCode))
                .collect(Collectors.toList())
                .get(0)
                .isAuthorized();
        Assertions.assertFalse(authorize2);
    }

    @Test
    @Order(300)
    void checkDeleteInPending() throws Exception {
        final Long userId = 89L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();

        // suer call
        Assertions.assertEquals(HttpStatus.NO_CONTENT, onboardingApi.addDeviceToPoolList(userId, deviceCode).getStatusCode());


        await().until(() -> {
            final DeviceEntity testResult = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);
            Assertions.assertNotNull(testResult);
            return testResult.getStatus() == DeviceStatus.PENDING;
        });


        final DeviceEntity testResult = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);
        final Long deviceId = testResult.getId();

        ResponseEntity<Void> statusResponse = deviceApi.delete(userId, deviceId);
        Assertions.assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        final DeviceEntity res = deviceRepository.findByFactoryCodeAndNotDeleted(deviceCode).orElse(null);
        Assertions.assertNull(res);

        boolean authorize = simulatorApi.getAllDeviceIds().getBody()
                .stream()
                .filter(devicePair -> devicePair.getCode().equals(deviceCode))
                .collect(Collectors.toList())
                .get(0)
                .isAuthorized();
        Assertions.assertFalse(authorize);
    }
}
