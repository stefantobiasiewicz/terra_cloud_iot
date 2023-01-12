package pl.terra.cloud_iot.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.TerraCloudIotApplication;
import pl.terra.cloud_simulator.TerraDeviceSimulatorApplication;


@ActiveProfiles("test")
@SpringBootTest(classes = {TerraCloudIotApplication.class, TerraDeviceSimulatorApplication.class})
public class MqttIntegrationTest extends IntegrationTestBase {
    @Test
    void context() {
        Assertions.assertTrue(true);
    }

}
