package pl.terra.cloud_iot.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest
public class MqttIntegrationTest extends IntegrationTestBase {


    @Test
    void context() {
        Assertions.assertTrue(true);
    }

}
