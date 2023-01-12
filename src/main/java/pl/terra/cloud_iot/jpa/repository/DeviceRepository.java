package pl.terra.cloud_iot.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.common.mqtt.DeviceMqtt;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    Optional<DeviceEntity> findByFactoryCode(final String code);

    @Query(value = "Select * form DeviceEntity d where d.toDeviceTopic = :#{#device.toDeviceTopic} and d.toServiceTopic = :#{#device.toServiceTopic}")
    Optional<DeviceEntity> findByDeviceMqtt(@Param("device") DeviceMqtt deviceMqtt);
}
