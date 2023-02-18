package pl.terra.cloud_simulator.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

@Component
public class ConfigLoader {

    private final File state;

    public ConfigLoader(@Value("${state-file:./}") final String stateFileDir) throws URISyntaxException, IOException {
        System.out.println("config loader path: " + stateFileDir);
        final File parentDirectory = new File(stateFileDir);
        state = new File(parentDirectory, "config.dat");
        state.getAbsolutePath();

        if (state.exists() != true) {
            state.createNewFile();
        }
    }

    public Map<String, Map<String, Object>> readState() {
        try (final FileInputStream fis = new FileInputStream(state);
             final ObjectInputStream is = new ObjectInputStream(fis)) {
            return (Map<String, Map<String, Object>>) is.readObject();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveState(final Map<String, Map<String, Object>> data) {
        try (final FileOutputStream fos = new FileOutputStream(state);
             final ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
