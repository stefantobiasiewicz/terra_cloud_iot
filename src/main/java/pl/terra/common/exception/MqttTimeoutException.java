package pl.terra.common.exception;

public class MqttTimeoutException extends SystemException {
    public MqttTimeoutException(String message) {
        super(message);
    }
}
