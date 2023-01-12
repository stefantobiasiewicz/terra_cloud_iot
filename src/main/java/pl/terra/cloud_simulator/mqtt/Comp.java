package pl.terra.cloud_simulator.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class Comp {
    private static Logger logger = LogManager.getLogger(Comp.class);

    public Comp() {
        logger.info("component created!!!");
    }
}
