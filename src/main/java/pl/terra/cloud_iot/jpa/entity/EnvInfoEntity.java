package pl.terra.cloud_iot.jpa.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "environment_info")
public class EnvInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    DeviceEntity device;

    @Column(name = "created")
    private LocalDateTime createdAt;

    @Column(name = "temp")
    private Double temp;

    @Column(name = "pres")
    private Double pres;

    @Column(name = "hum")
    private Double hum;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeviceEntity getDevice() {
        return device;
    }

    public void setDevice(DeviceEntity device) {
        this.device = device;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public Double getPres() {
        return pres;
    }

    public void setPres(Double pres) {
        this.pres = pres;
    }

    public Double getHum() {
        return hum;
    }

    public void setHum(Double hum) {
        this.hum = hum;
    }

    @Override
    public String toString() {
        return "EnvInfoEntity{" +
                "id=" + id +
                ", device=" + device +
                ", createdAt=" + createdAt +
                ", temp=" + temp +
                ", pres=" + pres +
                ", hum=" + hum +
                '}';
    }
}
