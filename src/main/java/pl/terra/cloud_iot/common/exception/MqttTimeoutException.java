package pl.terra.cloud_iot.common.exception;

public class MqttTimeoutException extends SystemException{
    public MqttTimeoutException(String message) {
        super(message);
    }
}
