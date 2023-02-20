package pl.terra.cloud_iot.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.terra.cloud_iot.jpa.entity.DeviceEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
    @Query(value = "Select d from DeviceEntity d where d.factoryCode = :code and d.status != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED")
    Optional<DeviceEntity> findByFactoryCodeAndActive(@Param("code") final String code);

    @Query(value = "Select d from DeviceEntity d where d.toDeviceTopic = :toDeviceTopic and d.toServiceTopic = :toServiceTopic and d.status != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED")
    Optional<DeviceEntity> findByDeviceMqtt(@Param("toDeviceTopic") String toDeviceTopic, @Param("toServiceTopic") String toServiceTopic);

    @Query(value = "Select d from DeviceEntity d where d.userId = :userId and d.status != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED")
    List<DeviceEntity> findAllByUserId(Long userId);

    @Query(value = "Select d from DeviceEntity d where d.userId = :userId and d.id = :deviceId and d.status != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED")
    List<DeviceEntity> findAllByUserIdAndIdAndActive(@Param("userId") Long userId, @Param("deviceId")  Long deviceId);

    @Query(value = "Select d from DeviceEntity d where d.status != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED")
    List<DeviceEntity> findAllActive();

//    @Query(value = "Select d from DeviceEntity d where d.userId = :userId and d.id = :deviceId and d.status != pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus.DELETED")
//    List<DeviceEntity> findAllActive(@Param("userId") Long userId, @Param("deviceId")  Long deviceId);
}
