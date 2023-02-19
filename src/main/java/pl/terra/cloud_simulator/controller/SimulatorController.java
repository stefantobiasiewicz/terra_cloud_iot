package pl.terra.cloud_simulator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.terra.cloud_simulator.model.DeviceModel;
import pl.terra.cloud_simulator.mqtt.DeviceMqttDriver;
import pl.terra.cloud_simulator.mqtt.MqttDispatcher;
import pl.terra.cloud_simulator.rng.RandomWithDelay;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.EnvInfo;
import pl.terra.device.model.MessageType;
import pl.terra.device.model.MqttSystemMessage;
import pl.terra.device.model.StatusResp;
import pl.terra.http.model.Connection;
import pl.terra.http.model.Device;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SimulatorController implements SimulatorApi, MqttDispatcher {
    private static final Logger logger = LogManager.getLogger(SimulatorController.class);
    private final ConfigLoader configLoader;
    private final DeviceMqttDriver deviceMqttDrive;
    private final String serverBaseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, String> devicesHardCoded = new HashMap<>();
    private Map<String, Map<String, Object>> cache = new HashMap<>();

    private Map<String, DeviceModel> cacheV2 = new HashMap<>();

    public SimulatorController(ConfigLoader configLoader, DeviceMqttDriver deviceMqttDrive, @Value("${simulator.backend.url}") final String serverPort, @Value("${example-devices}") final String pathToExamples) throws IOException, URISyntaxException {
        this.configLoader = configLoader;
        this.deviceMqttDrive = deviceMqttDrive;
        System.out.println("example devices file: " + pathToExamples);
        final File json = new File(pathToExamples);
        final List<String> deviceCodes = mapper.readValue(json, new TypeReference<List<String>>() {
        });

        Long i = 0L;
        for (final String device : deviceCodes) {
            devicesHardCoded.put(i++, device);
        }

        this.serverBaseUrl = serverPort;
        System.out.println(serverPort);

        Map<String, Map<String, Object>> state = configLoader.readState();
        if (state != null) {
            cache = state;
            // todo tutja dodac subskrybowanie topikow po odpaleniu
        }

        deviceMqttDrive.addDispatcher(this);
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
    @GetMapping("/device/get/all")
    public ResponseEntity<List<Map.Entry<Long, String>>> getAllDeviceIds() {
        return ResponseEntity.ok(new ArrayList<>(devicesHardCoded.entrySet()));
    }

    @Override
    @GetMapping("/device/get/code/{id}")
    public ResponseEntity<String> getDeviceCode(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(devicesHardCoded.get(id));
    }

    @Override
    @PostMapping("/device/authorize/{id}")
    public ResponseEntity<String> authorizeDevice(@PathVariable(name = "id") final Long id) throws SystemException {
        final String deviceCode = devicesHardCoded.get(id);

        final String url = String.format("%s/device/connection/%s", serverBaseUrl, deviceCode);

        Connection connection;
        try {
            connection = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(null), Connection.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(404).body("can't find device on backend");
            }
            return ResponseEntity.status(500).body("error calling backend");
        }


        final DeviceMqtt deviceMqtt = new DeviceMqtt();
        deviceMqtt.setId(id);
        deviceMqtt.setToDeviceTopic(connection.getToDeviceTopic());
        deviceMqtt.setToServiceTopic(connection.getToServiceTopic());

        DeviceModel model = new DeviceModel(deviceCode);
        model.setDeviceMqtt(deviceMqtt);
        cacheV2.put(deviceCode, model);

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

        deviceMqttDrive.registerService(deviceMqtt);
        deviceMqttDrive.sendToBackend(deviceMqtt, authorize);

        configLoader.saveState(cache);
        return null;
    }

    private DeviceModel getDeviceProperties(final DeviceMqtt deviceMqtt) {
        final String code = cacheV2.keySet().stream().filter(s -> cacheV2.get(s).getDeviceMqtt().equals(deviceMqtt))
                .findFirst().orElse(null);
        if (code == null) {
            SimulatorController.logger.error("can't find device: '{}' in cache: {}", deviceMqtt, cache);
            return null;
        }
        return cacheV2.get(code);
    }

    @Override
    public void handleMessage(DeviceMqtt device, MqttSystemMessage message) throws SystemException {
        switch (message.getType()) {
            case STATUS_REQ: {
                final DeviceModel deviceModel = getDeviceProperties(device);
                if (deviceModel == null) {
                    return;
                }

                final MqttSystemMessage response = new MqttSystemMessage();
                response.setMessageId(message.getMessageId());
                response.setType(MessageType.STATUS_RESP);

                final StatusResp statusResponse = deviceModel.asStatusResp();

                response.setPayload(statusResponse);
                deviceMqttDrive.sendToBackend(device, response);
            }
            break;
            default:
                break;
        }
    }
}
