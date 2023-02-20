package pl.terra.cloud_iot.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnvCheckTest extends IntegrationTestBase {

    @Autowired
    private DeviceRepository deviceRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    void beforeAll() throws URISyntaxException, IOException {
        final File json = new File(EnvCheckTest.class.getResource("/devices/excample_device_list.json").toURI());

        final List<String> deviceCodes = mapper.readValue(json, new TypeReference<List<String>>() {
        });

        Long userCounter = 1L;
        for (final String code : deviceCodes) {
            final DeviceEntity entity = new DeviceEntity();
            entity.setFactoryCode(code);
            entity.setStatus(DeviceStatus.READY);
            entity.setUserId(userCounter++);
            entity.setCreatedAt(LocalDate.now());
            entity.setToDeviceTopic(String.format("device/%s", code));
            entity.setToServiceTopic(String.format("service/%s", code));
            entity.setName("Name");

            deviceRepository.save(entity);
        }
    }

    @AfterAll
    void afterAll() {
        deviceRepository.deleteAll();
    }

    @Test
    void testGetDeviceByCode() {
        final DeviceEntity entity = deviceRepository.findByFactoryCodeAndActive("XW3V41AD26").orElse(null);

        Assertions.assertEquals(4, entity.getUserId());
    }

    @Test
    void testGetDeviceByTopics() {
        final DeviceEntity entity = deviceRepository.findByDeviceMqtt("device/CYM0YZJVJZ", "service/CYM0YZJVJZ").orElse(null);

        Assertions.assertEquals(1, entity.getUserId());
    }

}
