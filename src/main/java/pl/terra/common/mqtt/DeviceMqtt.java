package pl.terra.common.mqtt;

public class DeviceMqtt {

    private String toDeviceTopic;
    private String toServiceTopic;

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
}
