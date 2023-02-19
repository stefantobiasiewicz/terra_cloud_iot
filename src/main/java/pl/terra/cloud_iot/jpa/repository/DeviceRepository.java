package pl.terra.cloud_iot.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;
import pl.terra.common.mqtt.DeviceMqtt;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    Optional<DeviceEntity> findByFactoryCode(final String code);

    @Query(value = "Select d from DeviceEntity d where d.toDeviceTopic = :toDeviceTopic and d.toServiceTopic = :toServiceTopic")
    Optional<DeviceEntity> findByDeviceMqtt(@Param("toDeviceTopic") String toDeviceTopic, @Param("toServiceTopic") String toServiceTopic);

    List<DeviceEntity> findAllByUserId(Long userId);

    List<DeviceEntity> findAllByUserIdAndId(Long userId, Long deviceId);
}
