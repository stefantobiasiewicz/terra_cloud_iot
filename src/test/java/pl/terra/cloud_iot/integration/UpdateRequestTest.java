package pl.terra.cloud_iot.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.TerraCloudIotApplication;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_simulator.TerraDeviceSimulatorApplication;
import pl.terra.cloud_simulator.controller.SimulatorApi;
import pl.terra.http.api.DeviceApi;
import pl.terra.http.api.OnboardingApi;
import pl.terra.http.model.DeviceUpdate;
import pl.terra.http.model.Heater;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UpdateRequestTest extends IntegrationTestBase {
    @Autowired
    SimulatorApi simulatorApi;
    @Autowired
    OnboardingApi onboardingApi;
    @Autowired
    DeviceApi deviceApi;
    @Autowired
    DeviceRepository deviceRepository;


    @BeforeAll
    void prepareDevices() throws Exception {
        final Long userId = 19L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();

        // suer call
        Assertions.assertEquals(HttpStatus.NO_CONTENT, onboardingApi.addDeviceToPoolList(userId, deviceCode).getStatusCode());

        //device "start onboarding button"
        Assertions.assertEquals(HttpStatus.OK, simulatorApi.authorizeDevice(userId).getStatusCode());

        await().until(() -> {
            final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);
            Assertions.assertNotNull(testResult);
            return testResult.getStatus() == DeviceStatus.READY;
        });
    }

    @Test
    void checkUpdateRequest() throws Exception {
        final Long userId = 19L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();
        final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);

        Assertions.assertNotNull(testResult);
        final Long deviceId = testResult.getId();

        final Boolean fan = true;
        final Boolean humidifier = true;
        final Boolean light = true;

        final Heater heater = new Heater();
        heater.setOnOff(true);
        heater.setTemp(22.2);

        final DeviceUpdate deviceUpdate = new DeviceUpdate();
        deviceUpdate.setFan(fan);
        deviceUpdate.setHumidifier(humidifier);
        deviceUpdate.setLight(light);
        deviceUpdate.setHeater(heater);

        ResponseEntity<pl.terra.http.model.DeviceStatus> statusResponse = deviceApi.update(userId, deviceId, deviceUpdate);

        pl.terra.http.model.DeviceStatus status = statusResponse.getBody();

        Assertions.assertNotNull(status);
        Assertions.assertEquals(deviceId, status.getDevice().getId());
        Assertions.assertEquals(fan, status.getFan());
        Assertions.assertEquals(light, status.getLight());
        Assertions.assertEquals(humidifier, status.getHumidifier());
        Assertions.assertEquals(heater.getOnOff(), status.getHeater().getOnOff());
        Assertions.assertEquals(heater.getSetTemp(), status.getHeater().getSetTemp());
    }


    @Test
    void checkUpdateRequestNulls() throws Exception {
        final Long userId = 19L;
        final String deviceCode = simulatorApi.getDeviceCode(userId).getBody();
        final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);

        Assertions.assertNotNull(testResult);
        final Long deviceId = testResult.getId();

        pl.terra.http.model.DeviceStatus prestatus = deviceApi.get(userId, deviceId).getBody();

        final Boolean fan = null;
        final Boolean humidifier = true;
        final Boolean light = null;

        final Heater heater = new Heater();
        heater.setOnOff(true);
        heater.setTemp(22.2);

        final DeviceUpdate deviceUpdate = new DeviceUpdate();
        deviceUpdate.setFan(fan);
        deviceUpdate.setHumidifier(humidifier);
        deviceUpdate.setLight(light);
        deviceUpdate.setHeater(heater);

        ResponseEntity<pl.terra.http.model.DeviceStatus> statusResponse = deviceApi.update(userId, deviceId, deviceUpdate);

        pl.terra.http.model.DeviceStatus status = statusResponse.getBody();

        Assertions.assertNotNull(status);
        Assertions.assertEquals(deviceId, status.getDevice().getId());
        Assertions.assertEquals(prestatus.getFan(), status.getFan());
        Assertions.assertEquals(prestatus.getLight(), status.getLight());
        Assertions.assertEquals(humidifier, status.getHumidifier());
        Assertions.assertEquals(heater.getOnOff(), status.getHeater().getOnOff());
        Assertions.assertEquals(heater.getSetTemp(), status.getHeater().getSetTemp());
    }

}
