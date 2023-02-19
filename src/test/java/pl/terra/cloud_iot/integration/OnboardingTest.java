package pl.terra.cloud_iot.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.TerraCloudIotApplication;
import pl.terra.cloud_iot.domain.DeviceService;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.cloud_simulator.TerraDeviceSimulatorApplication;
import pl.terra.cloud_simulator.controller.SimulatorApi;
import pl.terra.cloud_simulator.mqtt.DeviceMqttDriver;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;
import pl.terra.http.api.OnboardingApi;
import pl.terra.http.model.Connection;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OnboardingTest extends IntegrationTestBase {
    @Autowired
    OnboardingApi onboardingApi;

    @Autowired
    ServiceMqttDriver serviceMqttDriver;

    @Autowired
    DeviceMqttDriver deviceMqttDriver;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    SimulatorApi simulatorApi;

    @Test
    void testManualOnboarding() throws Exception {
        final Long userId = 5L;
        final String deviceCode = "X0YWQ8AANY";

        // user call
        onboardingApi.addDeviceToPoolList(userId, deviceCode);

        // device call
        final Connection connection = onboardingApi.getConnection(deviceCode).getBody();
        Assertions.assertNotNull(connection);

        // device message send
        final DeviceMqtt deviceMqtt = new DeviceMqtt();
        deviceMqtt.setToDeviceTopic(connection.getToDeviceTopic());
        deviceMqtt.setToServiceTopic(connection.getToServiceTopic());

        MqttSystemMessage authorizeMessage = new MqttSystemMessage();
        authorizeMessage.setMessageId(35L);
        authorizeMessage.setType(MessageType.AUTHORIZE);
        authorizeMessage.setPayload(null);
        deviceMqttDriver.sendToBackend(deviceMqtt, authorizeMessage);

        await().until(() -> {
            final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);
            Assertions.assertNotNull(testResult);
            return testResult.getStatus() == DeviceStatus.READY;
        });

        final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);
        Assertions.assertNotNull(testResult);
        Assertions.assertEquals(DeviceStatus.READY, testResult.getStatus());
        Assertions.assertEquals(userId, testResult.getUserId());
        Assertions.assertEquals(connection.getToDeviceTopic(), testResult.getToDeviceTopic());
        Assertions.assertEquals(connection.getToServiceTopic(), testResult.getToServiceTopic());
    }

    @Test
    void testAutomaticOnboarding() throws Exception {
        final Long userId = 16L;
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

        final DeviceEntity testResult = deviceRepository.findByFactoryCode(deviceCode).orElse(null);
        Assertions.assertNotNull(testResult);
        Assertions.assertEquals(DeviceStatus.READY, testResult.getStatus());
        Assertions.assertEquals(userId, testResult.getUserId());
    }

}
