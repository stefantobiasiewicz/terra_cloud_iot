package pl.terra.cloud_iot.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.TerraCloudIotApplication;
import pl.terra.cloud_iot.mqtt.ServiceMqttDriver;
import pl.terra.cloud_simulator.TerraDeviceSimulatorApplication;
import pl.terra.cloud_simulator.mqtt.DeviceMqttDriver;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;


@ActiveProfiles("test")
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class})
public class MqttIntegrationTest extends IntegrationTestBase {

    @Autowired
    DeviceMqttDriver deviceMqttDriver;
    @Autowired
    ServiceMqttDriver serviceMqttDriver;

    @Test
    void context() {
        Assertions.assertTrue(true);
    }

    @Test
    void testExchangeMechanism() throws SystemException, InterruptedException {
        final DeviceMqtt testDeviceMqtt = new DeviceMqtt();
        testDeviceMqtt.setToDeviceTopic("/to/device/to");
        testDeviceMqtt.setToServiceTopic("/to/service/to");

        serviceMqttDriver.registerDevice(testDeviceMqtt);
        deviceMqttDriver.registerService(testDeviceMqtt);

        MqttSystemMessage message = new MqttSystemMessage();
        message.setMessageId(69L);
        message.setType(MessageType.PING);
        message.setPayload(null);

        MqttSystemMessage response = serviceMqttDriver.exchange(testDeviceMqtt, message, 10000L);

        Assertions.assertEquals(69L, response.getMessageId());
        Assertions.assertEquals(MessageType.OK, response.getType());

        Thread.sleep(2000);

        MqttSystemMessage response2 = serviceMqttDriver.exchange(testDeviceMqtt, message, 10000L);

        Assertions.assertEquals(69L, response2.getMessageId());
        Assertions.assertEquals(MessageType.OK, response2.getType());
    }

}
