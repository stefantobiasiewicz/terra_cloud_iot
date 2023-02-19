package pl.terra.cloud_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"pl.terra.common.*", "pl.terra.cloud_simulator.*"})
public class TerraDeviceSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication application =
                new SpringApplication(TerraDeviceSimulatorApplication.class);
        application.setAdditionalProfiles("sim");
        application.run(args);
    }
}
