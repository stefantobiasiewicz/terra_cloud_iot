package pl.terra.cloud_iot.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.terra.cloud_iot.jpa.entity.EnvInfoEntity;

public interface EnvInfoRepository extends JpaRepository<EnvInfoEntity, Long> {

}
