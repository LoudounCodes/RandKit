package org.loudouncodes.randkit.util;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * RNG utilities for RandKit.
 * <p>
 * Centralizes the default algorithm choice so distribution classes can depend on
 * a single place for creating generators. This keeps your public API stable even
 * if you later change the underlying algorithm.
 */
public final class Randoms {
    private Randoms() {}

    /**
     * Returns a platform-provided, high-quality default generator factory.
     * <p>
     * Currently uses the LXM family ("L64X128MixRandom") for a good balance of
     * quality and speed on modern JDKs.
     *
     * @return a {@link RandomGeneratorFactory} for the default algorithm
     */
    public static RandomGeneratorFactory<? extends RandomGenerator> defaultFactory() {
        return RandomGeneratorFactory.of("L64X128MixRandom");
    }

    /**
     * Creates a new default generator.
     *
     * @return a new {@link RandomGenerator} instance
     */
    public static RandomGenerator defaultGenerator() {
        return defaultFactory().create();
    }

    /**
     * Creates a new default generator seeded for deterministic reproducibility.
     *
     * @param seed seed value
     * @return a new {@link RandomGenerator} instance seeded with {@code seed}
     */
    public static RandomGenerator seeded(long seed) {
        return defaultFactory().create(seed);
    }
}
