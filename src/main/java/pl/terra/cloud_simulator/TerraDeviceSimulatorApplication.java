package pl.terra.cloud_simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"pl.terra.common.*", "pl.terra.cloud_simulator.*"})
public class TerraDeviceSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerraDeviceSimulatorApplication.class, args);
    }
}
