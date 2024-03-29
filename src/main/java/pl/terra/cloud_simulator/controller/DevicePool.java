package pl.terra.cloud_simulator.controller;

import pl.terra.cloud_simulator.model.DeviceModel;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DevicePool implements Serializable {
    private final Set<DeviceModel> pool;


    public DevicePool(final Set<DeviceModel> pool) {
        this.pool = pool;
    }

    public DeviceModel get(final String code) {
        return pool.stream()
                .filter(device -> device.getDeviceCode().equals(code))
                .findFirst()
                .orElse(null);
    }


    public static DevicePool formCodes(final List<String> list) {
        Set<DeviceModel> deviceSet = new LinkedHashSet<>();

        for (final String code : list) {
            deviceSet.add(new DeviceModel(code));
        }

        return new DevicePool(deviceSet);
    }

    public static DevicePool loadBinary(final File file) {
        try (final FileInputStream fis = new FileInputStream(file);
             final ObjectInputStream is = new ObjectInputStream(fis)) {
            return (DevicePool) is.readObject();
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveBinary(final File file, final DevicePool pool) {
        try (final FileOutputStream fos = new FileOutputStream(file);
             final ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(pool);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
