package pl.terra.cloud_simulator.controller;

import org.springframework.http.ResponseEntity;
import pl.terra.common.exception.SystemException;

import java.util.List;
import java.util.Map;

public interface SimulatorApi {
    ResponseEntity<List<Map.Entry<Long, String>>> getAllDeviceIds();

    ResponseEntity<String> getDeviceCode(Long id);

    ResponseEntity<String> authorizeDevice(Long id) throws SystemException;
}
