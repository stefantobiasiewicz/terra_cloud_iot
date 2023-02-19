package pl.terra.cloud_simulator.dto;

public class DevicePair {

    private Long id;
    private String code;

    private boolean authorized;

    public DevicePair(final Long id, final String code, final boolean authorized) {
        this.id = id;
        this.code = code;
        this.authorized =authorized;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public boolean isAuthorized() {
        return authorized;
    }
}
