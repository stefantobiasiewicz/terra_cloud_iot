package pl.terra.cloud_iot.jpa.entity.enums;

public enum DeviceStatus {
    PENDING(1, "PENDING"),
    READY(2, "READY");
    private final int id;
    private final String value;

    DeviceStatus(int id, String value) {
        this.id = id;
        this.value = value;
    }

    DeviceStatus valueFrom(final String value) {
        for (DeviceStatus e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("can't create DeviceStatus form value: '%s'", value));
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
