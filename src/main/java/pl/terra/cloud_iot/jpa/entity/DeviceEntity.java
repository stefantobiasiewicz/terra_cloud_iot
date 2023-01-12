package pl.terra.cloud_iot.jpa.entity;

import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;

import javax.persistence.*;
import java.util.Date;

@Table(name = "devices")
@Entity
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "factory_code", nullable = false, unique = true)
    private String factoryCode;
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;
    @Column(name = "topic_to_device", nullable = false)
    private String toDeviceTopic;
    @Column(name = "topic_to_service", nullable = false)
    private String toServiceTopic;

    public Long getId() {
        return id;
    }
}
