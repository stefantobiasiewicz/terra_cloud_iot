package pl.terra.cloud_simulator.rng;

import java.util.Random;

public class RandomWithDelay {
    private Double prevGeneration = 0.;
    private Double factor = 0.5;

    public RandomWithDelay(Double factor) {
        this.factor = factor;
    }

    public Double getRandom(final Double predict) {
        Random r = new Random();
        double randomValue =  r.nextDouble();

        final Double result = randomValue * factor + prevGeneration;
        prevGeneration = result;
        return result;
    }
}
