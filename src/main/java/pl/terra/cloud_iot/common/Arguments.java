package pl.terra.cloud_iot.common;

import pl.terra.cloud_iot.common.exception.SystemException;

public final class Arguments {

    public static void isNull(final Object o, final String objectName) throws SystemException {
        if(o == null) {
            throw new SystemException(String.format("object: '%s' is null.", objectName));
        }
    }

    public static void isNullOrEmpty(final String o, final String stringName) throws SystemException {
        if(o == null) {
            throw new SystemException(String.format("string: '%s' is null.", stringName));
        }
        if(o.isEmpty() || o.isBlank()) {
            throw new SystemException(String.format("string: '%s' is empty or blank.", stringName));
        }
    }

    public Arguments() {
        throw new IllegalArgumentException("this is utils class and it can't be created");
    }
}
