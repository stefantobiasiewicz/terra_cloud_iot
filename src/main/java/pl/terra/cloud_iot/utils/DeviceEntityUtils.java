package pl.terra.cloud_iot.utils;

import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;
import pl.terra.http.model.Device;

import java.net.URI;

public class DeviceEntityUtils {

    public static Device mapToDevice(DeviceEntity device) {
        final Device dev = new Device();
        dev.setId(device.getId());
        dev.setName(device.getName());
        if(device.getImage()!=null) {
            dev.setImage(URI.create(device.getImage()));
        }
        dev.setStatus(Device.StatusEnum.valueOf(device.getStatus().getValue()));
        return dev;
    }
}
