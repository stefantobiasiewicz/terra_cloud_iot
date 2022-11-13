package pl.terra.cloud_iot.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import pl.terra.cloud_iot.jpa.entity.Device;
import pl.terra.cloud_iot.jpa.repository.DeviceRepository;

@ActiveProfiles("test")
@SpringBootTest
public class EnvCheckTest extends IntegrationTestBase {

    @Autowired
    DeviceRepository deviceRepository;

    @Test
    public void testProductionDevicesDataCheck() {
        Device device = deviceRepository.findByFactoryCode("AXBECWQ12").orElse(null);
        Assertions.assertEquals(1L, device.getId());

        device = deviceRepository.findByFactoryCode("BXDW13GAS").orElse(null);
        Assertions.assertEquals(2L, device.getId());

        device = deviceRepository.findByFactoryCode("THOXJQWGZ").orElse(null);
        Assertions.assertEquals(3L, device.getId());

        device = deviceRepository.findByFactoryCode("OBPSIRNAP").orElse(null);
        Assertions.assertEquals(4L, device.getId());

        device = deviceRepository.findByFactoryCode("QMNRJCANN").orElse(null);
        Assertions.assertEquals(5L, device.getId());

        device = deviceRepository.findByFactoryCode("OOPSIWNQ2").orElse(null);
        Assertions.assertEquals(6L, device.getId());
    }

    @Test
    @Sql("classpath:sql/devices.sql")
    public void testAdditionalScriptDevicesDataCheck() {
        Device device = deviceRepository.findByFactoryCode("MBSAHEQCA").orElse(null);
        Assertions.assertEquals(101L, device.getId());

        device = deviceRepository.findByFactoryCode("MRIASNFQQ").orElse(null);
        Assertions.assertEquals(102L, device.getId());

        device = deviceRepository.findByFactoryCode("ASDSJVBRB").orElse(null);
        Assertions.assertEquals(103L, device.getId());

        device = deviceRepository.findByFactoryCode("ADSAKGEWS").orElse(null);
        Assertions.assertEquals(104L, device.getId());

        device = deviceRepository.findByFactoryCode("LLBAFASDS").orElse(null);
        Assertions.assertEquals(105L, device.getId());

        device = deviceRepository.findByFactoryCode("VBEWNIWNA").orElse(null);
        Assertions.assertEquals(106L, device.getId());
    }
}
