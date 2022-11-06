package pl.terra.cloud_iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.terra.device.model.LightCmd;
import pl.terra.device.model.MqttMessage;

import java.io.File;
import java.io.IOException;

@SpringBootTest
class TerraCloudIotApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    void testMapping() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        final File json = new File("src/test/resources/ExcampleMessage.json");
        final MqttMessage message = mapper.readValue(json, MqttMessage.class);

        Assertions.assertEquals(1, message.getMessageId());

        final LightCmd lightCmd = mapper.readValue(mapper.writeValueAsString(message.getPayload()), LightCmd.class);

        Assertions.assertTrue(lightCmd.getOnOff());
    }


}
