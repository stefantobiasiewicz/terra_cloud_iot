package pl.terra.cloud_simulator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import pl.terra.cloud_simulator.mqtt.DeviceMqttDriver;
import pl.terra.cloud_simulator.rng.RandomWithDelay;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.EnvInfo;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;
import pl.terra.http.model.Connection;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@RestController
public class SimulatorController implements SimulatorApi {

    private final ConfigLoader configLoader;
    private final DeviceMqttDriver deviceMqttDrive;
    private final String serverBaseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, String> devices = new HashMap<>();
    private Map<String, Map<String, Object>> cache = new HashMap<>();

    public SimulatorController(ConfigLoader configLoader, DeviceMqttDriver deviceMqttDrive, @Value("${simulator.backend.url}") final String serverPort) throws IOException, URISyntaxException {
        this.configLoader = configLoader;
        this.deviceMqttDrive = deviceMqttDrive;
        final File json = new File(SimulatorController.class.getResource("/devices/excample_device_list.json").toURI());
        final List<String> deviceCodes = mapper.readValue(json, new TypeReference<List<String>>() {
        });

        Long i = 0L;
        for (final String device : deviceCodes) {
            devices.put(i++, device);
        }

        this.serverBaseUrl = serverPort;
        System.out.println(serverPort);

        Map<String, Map<String, Object>> state = configLoader.readState();
        if (state != null) {
            cache = state;
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() throws SystemException {
        for (final String deviceCode : cache.keySet()) {
            final Map<String, Object> deviceConfig = cache.get(deviceCode);
            final DeviceMqtt deviceMqtt = (DeviceMqtt) deviceConfig.get("device");

            final MqttSystemMessage message = new MqttSystemMessage();
            message.setMessageId(0L);
            message.setType(MessageType.ENV_INFO);


            final EnvInfo envInfo = new EnvInfo();
            final RandomWithDelay tempRng = (RandomWithDelay) deviceConfig.get("temp rng");
            envInfo.setTemperature(tempRng.getRandom((Double) deviceConfig.get("temp")));

            final RandomWithDelay humRng = (RandomWithDelay) deviceConfig.get("hum rng");
            envInfo.setHumidity(humRng.getRandom((Double) deviceConfig.get("hum")));

            final RandomWithDelay presRng = (RandomWithDelay) deviceConfig.get("pres rng");
            envInfo.setPressure(presRng.getRandom((Double) deviceConfig.get("pres")));

            message.setPayload(envInfo);

            deviceMqttDrive.sendToBackend(deviceMqtt, message);
        }
    }


    @Override
    public ResponseEntity<List<Long>> getAllDeviceIds() {
        return ResponseEntity.ok(new ArrayList<>(devices.keySet()));
    }

    @Override
    @GetMapping("/device/get/code/{id}")
    public ResponseEntity<String> getDeviceCode(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(devices.get(id));
    }

    @Override
    @PostMapping("/device/authorize/{id}")
    public ResponseEntity<Void> authorizeDevice(@PathVariable(name = "id") final Long id) throws SystemException {
        final String deviceCode = devices.get(id);

        final String url = String.format("%s/device/connection/%s", serverBaseUrl, deviceCode);
        final Connection connection = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(null), Connection.class).getBody();

        final DeviceMqtt deviceMqtt = new DeviceMqtt();
        deviceMqtt.setToDeviceTopic(connection.getToDeviceTopic());
        deviceMqtt.setToServiceTopic(connection.getToServiceTopic());

        cache.put(deviceCode, new HashMap<>());
        cache.get(deviceCode).put("device", deviceMqtt);

        cache.get(deviceCode).put("temp rng", new RandomWithDelay(0.3).setStartValue(22.5));
        cache.get(deviceCode).put("hum rng", new RandomWithDelay(0.5).setStartValue(50.5));
        cache.get(deviceCode).put("pres rng", new RandomWithDelay(0.9).setStartValue(990.60));

        cache.get(deviceCode).put("temp", Double.valueOf(22.5));
        cache.get(deviceCode).put("hum", Double.valueOf(50.5));
        cache.get(deviceCode).put("pres", Double.valueOf(990.60));

        final MqttSystemMessage authorize = new MqttSystemMessage();
        authorize.setMessageId(0L);
        authorize.setType(MessageType.AUTHORIZE);
        authorize.setPayload(null);

        deviceMqttDrive.sendToBackend(deviceMqtt, authorize);

        configLoader.saveState(cache);
        return null;
    }
}
