package pl.terra.cloud_simulator.controller;

import org.springframework.http.ResponseEntity;
import pl.terra.common.exception.SystemException;

import java.util.List;

public interface SimulatorApi {
    ResponseEntity<List<Long>> getAllDeviceIds();

    ResponseEntity<String> getDeviceCode(Long id);

    ResponseEntity<Void> authorizeDevice(Long id) throws SystemException;
}
