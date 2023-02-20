package pl.terra.cloud_simulator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import pl.terra.cloud_simulator.dto.DevicePair;
import pl.terra.cloud_simulator.model.DeviceModel;
import pl.terra.common.exception.SystemException;

import java.util.List;

public interface SimulatorApi {
    ResponseEntity<List<DevicePair>> getAllDeviceIds();

    ResponseEntity<String> getDeviceCode(Long id);

    ResponseEntity<DeviceModel> getDeviceStatus(@PathVariable(name = "id") Long id);

    ResponseEntity<DeviceModel> setDeviceStatus(@RequestBody DeviceModel model);

    ResponseEntity<?> authorizeDevice(Long id) throws SystemException;
}
