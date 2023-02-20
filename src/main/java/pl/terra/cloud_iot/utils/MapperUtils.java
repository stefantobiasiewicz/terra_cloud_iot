package pl.terra.cloud_iot.utils;

import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.Heater;
import pl.terra.device.model.StatusResp;
import pl.terra.device.model.UpdateRequest;
import pl.terra.http.model.Device;
import pl.terra.http.model.DeviceStatus;
import pl.terra.http.model.DeviceUpdate;
import pl.terra.http.model.EnvInfo;

import java.net.URI;

public class MapperUtils {

    public MapperUtils() {
        throw new IllegalStateException("utils class");
    }

    public static Device mapToDevice(DeviceEntity device) {
        final Device dev = new Device();
        dev.setId(device.getId());
        dev.setName(device.getName());
        if (device.getImage() != null) {
            dev.setImage(URI.create(device.getImage()));
        }
        dev.setStatus(Device.StatusEnum.valueOf(device.getStatus().getValue()));
        return dev;
    }

    public static DeviceMqtt mapToMqttDevice(DeviceEntity entity) {
        final DeviceMqtt deviceMqtt = new DeviceMqtt();
        deviceMqtt.setToServiceTopic(entity.getToServiceTopic());
        deviceMqtt.setToDeviceTopic(entity.getToDeviceTopic());
        return deviceMqtt;
    }

    public static DeviceStatus mapToDeviceStatus(StatusResp statusResp, DeviceEntity device) {
        final DeviceStatus result = new DeviceStatus();
        result.setDevice(MapperUtils.mapToDevice(device));
        result.setDoors(statusResp.getDoors());
        result.setFan(statusResp.getFan());
        result.setLight(statusResp.getLight());

        final pl.terra.http.model.Heater heater = new pl.terra.http.model.Heater();
        heater.setOnOff(statusResp.getHeater().getOnOff());
        heater.setTemp(statusResp.getHeater().getSetTemp());
        result.setHeater(heater);
        result.setHumidifier(statusResp.getHumidifier());

        final EnvInfo envInfo = new EnvInfo();
        envInfo.setHumidity(statusResp.getEnvInfo().getHumidity());
        envInfo.setPressure(statusResp.getEnvInfo().getPressure());
        envInfo.setTemperature(statusResp.getEnvInfo().getTemperature());
        result.setEnvInfo(envInfo);

        return result;
    }

    public static UpdateRequest mapToUpdateRequest(DeviceUpdate deviceUpdate) {
        final UpdateRequest result = new UpdateRequest();
        result.setFan(deviceUpdate.getFan());

        final Heater heater = new Heater();
        heater.setOnOff(deviceUpdate.getHeater().getOnOff());
        heater.setTemp(deviceUpdate.getHeater().getSetTemp());

        result.setHeater(heater);
        result.setHumidifier(deviceUpdate.getHumidifier());
        result.setLight(deviceUpdate.getLight());

        return result;
    }
}
