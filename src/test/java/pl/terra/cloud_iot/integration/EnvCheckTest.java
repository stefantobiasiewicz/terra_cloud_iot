package pl.terra.cloud_iot.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;

@ActiveProfiles("test")
@SpringBootTest
public class EnvCheckTest extends IntegrationTestBase {

    @Autowired
    DeviceRepository deviceRepository;

}
