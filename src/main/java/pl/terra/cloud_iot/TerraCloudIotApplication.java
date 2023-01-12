package pl.terra.cloud_iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"pl.terra.common.*", "pl.terra.cloud_iot.*"})
public class TerraCloudIotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerraCloudIotApplication.class, args);
    }

}
