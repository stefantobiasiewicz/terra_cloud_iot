package pl.terra.cloud_iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.terra.device.model.MqttMessage;

@SpringBootApplication
public class TerraCloudIotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerraCloudIotApplication.class, args);
    }

    private void test() {
        MqttMessage message = new MqttMessage();
        message.setMessageId(1);
        message.setType("asasad");
    }
}
