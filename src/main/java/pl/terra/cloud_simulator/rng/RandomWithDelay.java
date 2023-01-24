package pl.terra.cloud_simulator.rng;

import java.io.Serializable;
import java.util.Random;

public class RandomWithDelay implements Serializable {
    private static final long serialVersionUID = 1231231231L;

    private Double prevGeneration = 0.;
    private Double factor = 0.5;

    public RandomWithDelay(Double factor) {
        this.factor = factor;
    }

    public RandomWithDelay setStartValue(Double value) {
        prevGeneration = value;
        return this;
    }

    public Double getRandom(final Double predict) {
        Random r = new Random();
        double randomValue =  (double)r.nextInt( 100)/100;

        double delta = (predict - prevGeneration) * randomValue;

        double noise = randomValue * 2;

        final Double result = prevGeneration + delta + noise;
        prevGeneration = result;
        return result;
    }
}
