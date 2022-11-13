package pl.terra.cloud_iot.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public abstract class IntegrationTestBase {
    public static final GenericContainer<?> MOSQUITO;
    public static final Integer mqttPort;

    public static final PostgreSQLContainer<?> POSTGRES;
    public static final String postgresUrl;
    public static final String postgresUser;
    public static final String postgresPassword;

    static {
        MOSQUITO = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:latest"))
                .withExposedPorts(1883)
                .withCopyFileToContainer(MountableFile.forClasspathResource("mqtt/mosquitto.conf"),"/mosquitto/config/mosquitto.conf")
                .withCopyFileToContainer(MountableFile.forClasspathResource("mqtt/mosquitto.passwd"),"/mosquitto/config/mosquitto.passwd");
        MOSQUITO.start();

        mqttPort = MOSQUITO.getMappedPort(1883);

        POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withDatabaseName("cloud_iot")
                .withUsername("postgres")
                .withPassword("postgres");
        POSTGRES.start();

        postgresUrl = POSTGRES.getJdbcUrl();
        postgresUser = POSTGRES.getUsername();
        postgresPassword = POSTGRES.getPassword();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("mqtt.broker", () -> "tcp://localhost:" + mqttPort);

        registry.add("spring.datasource.url", () -> postgresUrl);
        registry.add("spring.datasource.username", () -> postgresUser);
        registry.add("spring.datasource.password", () -> postgresPassword);
    }
}
