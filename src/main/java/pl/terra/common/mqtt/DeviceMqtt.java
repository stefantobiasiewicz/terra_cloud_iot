package pl.terra.common.mqtt;

import java.io.Serializable;

public class DeviceMqtt implements Serializable {
    private Long id;
    private String toDeviceTopic;
    private String toServiceTopic;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "DeviceMqtt{" +
                "toDeviceTopic='" + toDeviceTopic + '\'' +
                ", toServiceTopic='" + toServiceTopic + '\'' +
                '}';
    }
}
