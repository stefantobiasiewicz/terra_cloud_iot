package pl.terra.cloud_iot.jpa.entity;

import pl.terra.cloud_iot.jpa.entity.enums.DeviceStatus;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "devices")
@Entity
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "factory_code", nullable = false)
    private String factoryCode;
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;
    @Column(name = "topic_to_device", nullable = false)
    private String toDeviceTopic;
    @Column(name = "topic_to_service", nullable = false)
    private String toServiceTopic;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image", nullable = true)
    private String image;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFactoryCode() {
        return factoryCode;
    }

    public void setFactoryCode(String factoryCode) {
        this.factoryCode = factoryCode;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public String getToDeviceTopic() {
        return toDeviceTopic;
    }

    public void setToDeviceTopic(String toDeviceTopic) {
        this.toDeviceTopic = toDeviceTopic;
    }

    public String getToServiceTopic() {
        return toServiceTopic;
    }

    public void setToServiceTopic(String toServiceTopic) {
        this.toServiceTopic = toServiceTopic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "DeviceEntity{" +
                "id=" + id +
                ", factoryCode='" + factoryCode + '\'' +
                ", createdAt=" + createdAt +
                ", userId=" + userId +
                ", status=" + status +
                ", toDeviceTopic='" + toDeviceTopic + '\'' +
                ", toServiceTopic='" + toServiceTopic + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
