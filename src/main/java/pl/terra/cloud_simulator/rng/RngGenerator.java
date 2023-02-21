package pl.terra.cloud_simulator.rng;

import java.util.Random;

public final class RngGenerator {

    public static Double getNoise() {
        Random r = new Random();
        final Double result =  r.nextBoolean() ? r.nextDouble() : -r.nextDouble();
        return result * 0.5;
    }

    public static Double getRandom01() {
        Random r = new Random();
        return r.nextDouble();
    }

}
