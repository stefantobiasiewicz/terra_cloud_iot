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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.terra.cloud_simulator.dto.DevicePair;
import pl.terra.cloud_simulator.model.DeviceModel;
import pl.terra.cloud_simulator.mqtt.DeviceMqttDriver;
import pl.terra.cloud_simulator.mqtt.MqttDispatcher;
import pl.terra.common.exception.SystemException;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.*;
import pl.terra.http.model.Connection;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@RestController
public class SimulatorController implements SimulatorApi, MqttDispatcher {
    private static final Logger logger = LogManager.getLogger(SimulatorController.class);
    private final ConfigLoader configLoader;
    private final DeviceMqttDriver deviceMqttDrive;
    private final String serverBaseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, String> devicesHardCoded = new HashMap<>();

    private Map<String, DeviceModel> cacheV2 = new HashMap<>();

    public SimulatorController(ConfigLoader configLoader, DeviceMqttDriver deviceMqttDrive, @Value("${simulator.backend.url}") final String serverPort, @Value("${example-devices}") final String pathToExamples, RestTemplate restTemplate) throws IOException, URISyntaxException, SystemException {
        this.configLoader = configLoader;
        this.deviceMqttDrive = deviceMqttDrive;
        this.restTemplate = restTemplate;
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

        Map<String, DeviceModel> state = configLoader.readState();
        if (state != null) {
            cacheV2 = state;

            for (final String deviceCode : cacheV2.keySet()) {
                deviceMqttDrive.registerService(cacheV2.get(deviceCode).getDeviceMqtt());
            }
        }

        deviceMqttDrive.addDispatcher(this);
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() throws SystemException {
        for (final String deviceCode : cacheV2.keySet()) {
            final DeviceModel deviceConfig = cacheV2.get(deviceCode);
            final DeviceMqtt deviceMqtt = deviceConfig.getDeviceMqtt();

            final MqttSystemMessage message = new MqttSystemMessage();
            message.setMessageId(0L);
            message.setType(MessageType.ENV_INFO);


            final EnvInfo envInfo = new EnvInfo();
            envInfo.setTemperature(deviceConfig.getTemperature());

            envInfo.setHumidity(deviceConfig.getHumidity());

            envInfo.setPressure(deviceConfig.getPressure());

            message.setPayload(envInfo);

            deviceMqttDrive.sendToBackend(deviceMqtt, message);
        }
    }


    @Scheduled(fixedDelay = 10000)
    public void saveCache() throws SystemException {
        SimulatorController.logger.info("saving cache!");
        configLoader.saveState(cacheV2);
    }

    @Override
    @GetMapping("/device/get/all")
    public ResponseEntity<List<DevicePair>> getAllDeviceIds() {
        List<DevicePair> devicePairs = new ArrayList<>();

        devicesHardCoded.forEach((aLong, s) -> {
            DeviceModel model = cacheV2.get(s);
            if (model != null) {
                devicePairs.add(new DevicePair(aLong, s, true));
            } else {
                devicePairs.add(new DevicePair(aLong, s, false));
            }
        });

        return ResponseEntity.ok(devicePairs);
    }

    @Override
    @GetMapping("/device/get/code/{id}")
    public ResponseEntity<String> getDeviceCode(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(devicesHardCoded.get(id));
    }

    @GetMapping("/device/get/code/json/{id}")
    public ResponseEntity<?> getDeviceCodeJson(@PathVariable(name = "id") final Long id) {
        Map<String, String> map = new HashMap<>();
        map.put("code", devicesHardCoded.get(id));

        return ResponseEntity.ok(map);
    }

    @Override
    @GetMapping("/device/get/status/{id}")
    public ResponseEntity<DeviceModel> getDeviceStatus(@PathVariable(name = "id") final Long id) {
        final DeviceModel response = cacheV2.get(devicesHardCoded.get(id));
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    @PostMapping("/device/get/status/{id}")
    public ResponseEntity<DeviceModel> setDeviceStatus(@RequestBody final DeviceModel model) {
        final DeviceModel response = cacheV2.get(model.getDeviceCode());
        if (response != null) {
            cacheV2.put(model.getDeviceCode(), model);
            return ResponseEntity.ok(cacheV2.get(model.getDeviceCode()));
        }
        return ResponseEntity.notFound().build();
    }


    @Override
    @PostMapping("/device/authorize/{id}")
    public ResponseEntity<?> authorizeDevice(@PathVariable(name = "id") final Long id) throws SystemException {
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

        final MqttSystemMessage authorize = new MqttSystemMessage();
        authorize.setMessageId(0L);
        authorize.setType(MessageType.AUTHORIZE);
        authorize.setPayload(null);

        deviceMqttDrive.registerService(deviceMqtt);
        deviceMqttDrive.sendToBackend(deviceMqtt, authorize);

        configLoader.saveState(cacheV2);
        return ResponseEntity.ok(model);
    }

    private DeviceModel getDeviceProperties(final DeviceMqtt deviceMqtt) {
        final String code = cacheV2.keySet().stream()
                .filter(s -> Objects.equals(cacheV2.get(s).getDeviceMqtt().getId(), deviceMqtt.getId()))
                .findFirst().orElse(null);
        if (code == null) {
            SimulatorController.logger.error("can't find device: '{}' in cache: {}", deviceMqtt, cacheV2);
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
                break;
            }
            case UPDATE_REQ: {
                final DeviceModel deviceModel = getDeviceProperties(device);
                if (deviceModel == null) {
                    return;
                }

                UpdateRequest request = DeviceMqttDriver.getPayloadClass(message, UpdateRequest.class);

                if (request.getFan() != null) {
                    deviceModel.setFanOnOff(request.getFan());
                }
                if (request.getHumidifier() != null) {
                    deviceModel.setHumidifierOnOff(request.getHumidifier());
                }
                if (request.getLight() != null) {
                    deviceModel.setLightOnOff(request.getLight());
                }
                if (request.getHeater() != null) {
                    if(request.getHeater().getOnOff() != null) {
                        deviceModel.setHeaterOnOff(request.getHeater().getOnOff());
                    }
                    if(request.getHeater().getSetTemp() != null) {
                        deviceModel.setHeaterSetTemp(request.getHeater().getSetTemp());
                    }
                }
                cacheV2.put(deviceModel.getDeviceCode(), deviceModel);

                final MqttSystemMessage response = new MqttSystemMessage();
                response.setMessageId(message.getMessageId());
                response.setType(MessageType.STATUS_RESP);

                final StatusResp statusResponse = deviceModel.asStatusResp();

                response.setPayload(statusResponse);
                deviceMqttDrive.sendToBackend(device, response);
                break;
            }
            default:
                break;
        }
    }
}
