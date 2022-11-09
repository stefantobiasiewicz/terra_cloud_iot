package pl.terra.cloud_iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.terra.cloud_iot.common.exception.SystemException;
import pl.terra.cloud_iot.mqtt.Device;
import pl.terra.cloud_iot.mqtt.MqttCore;
import pl.terra.device.model.EnvInfo;
import pl.terra.device.model.LightCmd;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;

import java.io.File;
import java.io.IOException;

@SpringBootTest
class TerraCloudIotApplicationTests {


    @Autowired
    MqttCore mqttCore;

    @Test
    void contextLoads() {
    }

    @Test
    void testMapping() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        final File json = new File("src/test/resources/ExcampleMessage.json");
        final MqttSystemMessage message = mapper.readValue(json, MqttSystemMessage.class);

        Assertions.assertEquals(1, message.getMessageId());

        final LightCmd lightCmd = mapper.readValue(mapper.writeValueAsString(message.getPayload()), LightCmd.class);

        Assertions.assertTrue(lightCmd.getOnOff());
    }

    @Test
    @Disabled
    void testMqtt() throws SystemException {
        final Device device = new Device();

        device.setToDeviceTopic("id/device");
        device.setToServiceTopic("id/service");

        mqttCore.registerDevice(device);

        MqttSystemMessage mqttSystemMessage = new MqttSystemMessage();
        mqttSystemMessage.setMessageId(69L);
        mqttSystemMessage.setType(MessageType.ENV_INFO);
        EnvInfo envInfo = new EnvInfo();
        envInfo.setHumidity(123.4);
        envInfo.setPressure(999.13);
        envInfo.setTemperature(23.3);
        mqttSystemMessage.setPayload(envInfo);

        MqttSystemMessage response = mqttCore.exchange(device, mqttSystemMessage, 10000L);

        Assertions.assertEquals(69L, response.getMessageId());
    }


}
