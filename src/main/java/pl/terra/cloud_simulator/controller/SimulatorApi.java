package pl.terra.cloud_simulator.controller;

import org.springframework.http.ResponseEntity;
import pl.terra.cloud_simulator.dto.DevicePair;
import pl.terra.common.exception.SystemException;

import java.util.List;

public interface SimulatorApi {
    ResponseEntity<List<DevicePair>> getAllDeviceIds();

    ResponseEntity<String> getDeviceCode(Long id);

    ResponseEntity<String> authorizeDevice(Long id) throws SystemException;
}
