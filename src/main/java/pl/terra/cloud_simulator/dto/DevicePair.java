package pl.terra.cloud_simulator.dto;

public class DevicePair {

    private Long id;
    private String code;

    public DevicePair(final Long id, final String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
