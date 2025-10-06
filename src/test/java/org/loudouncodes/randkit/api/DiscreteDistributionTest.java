package org.loudouncodes.randkit.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract checks for the DiscreteDistribution interface using a simple
 * degenerate distribution (point mass at k0) implemented locally for tests.
 */
class DiscreteDistributionTest {

    /** Simple point-mass distribution at k0: P(X=k0)=1, otherwise 0. */
    private static final class Degenerate implements DiscreteDistribution {
        private final int k0;
        Degenerate(int k0) { this.k0 = k0; }

        @Override public int sample() { return k0; }
        @Override public double pmf(int k) { return (k == k0) ? 1.0 : 0.0; }
        @Override public double cdf(int k) { return (k < k0) ? 0.0 : 1.0; }
        @Override public double mean() { return (double) k0; }
        @Override public double variance() { return 0.0; }
    }

    @Test
    @DisplayName("Degenerate distribution: pmf/cdf/mean/variance are consistent")
    void degenerateBasics() {
        DiscreteDistribution d = new Degenerate(7);

        // pmf
        assertEquals(1.0, d.pmf(7), 0.0);
        assertEquals(0.0, d.pmf(6), 0.0);
        assertEquals(0.0, d.pmf(8), 0.0);

        // cdf
        assertEquals(0.0, d.cdf(6), 0.0);
        assertEquals(1.0, d.cdf(7), 0.0);
        assertEquals(1.0, d.cdf(1000), 0.0);

        // moments
        assertEquals(7.0, d.mean(), 0.0);
        assertEquals(0.0, d.variance(), 0.0);
    }

    @Test
    @DisplayName("Sampling returns support value; deterministic by construction")
    void samplingDeterminism() {
        DiscreteDistribution d = new Degenerate(-3);
        for (int i = 0; i < 10_000; i++) {
            assertEquals(-3, d.sample());
        }
    }

    @Test
    @DisplayName("CDF is monotone and bounded in [0,1]")
    void cdfMonotoneAndBounded() {
        DiscreteDistribution d = new Degenerate(0);
        double prev = -1.0;
        for (int k = -5; k <= 5; k++) {
            double F = d.cdf(k);
            assertTrue(F >= 0.0 && F <= 1.0, "CDF out of bounds: " + F);
            assertTrue(F >= prev, "CDF not monotone at k=" + k);
            prev = F;
        }
    }
}
