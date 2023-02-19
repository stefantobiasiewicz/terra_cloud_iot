package pl.terra.cloud_iot.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.terra.cloud_iot.jpa.entity.EnvInfoEntity;

@Repository
public interface EnvInfoRepository extends JpaRepository<EnvInfoEntity, Long> {

}
