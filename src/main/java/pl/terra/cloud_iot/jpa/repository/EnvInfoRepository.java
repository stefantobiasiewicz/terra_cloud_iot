package pl.terra.cloud_iot.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.cloud_iot.jpa.entity.EnvInfoEntity;

import java.util.List;

@Repository
public interface EnvInfoRepository extends JpaRepository<EnvInfoEntity, Long> {
    List<EnvInfoEntity> findAllByDevice(DeviceEntity device);

    List<EnvInfoEntity> findAllByDevice(DeviceEntity device, Pageable pageable);
}
