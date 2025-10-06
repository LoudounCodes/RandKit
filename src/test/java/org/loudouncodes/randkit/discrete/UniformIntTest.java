package org.loudouncodes.randkit.discrete;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UniformIntTest {

    @Test
    @DisplayName("Constructor validation and degenerate case")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new UniformInt(5, 4));
        // Degenerate: always returns a when a==b
        UniformInt d = new UniformInt(7, 7);
        for (int i = 0; i < 1000; i++) {
            assertEquals(7, d.sample());
        }
        assertEquals(1.0, d.pmf(7), 0.0);
        assertEquals(0.0, d.pmf(6), 0.0);
        assertEquals(1.0, d.cdf(7), 0.0);
        assertEquals(0.0, d.cdf(6), 0.0);
    }

    @Test
    @DisplayName("Sampling stays within [a,b] inclusive and is deterministic by seed")
    void boundsAndDeterminism() {
        int a = -3, b = 2;
        UniformInt d1 = new UniformInt(123456789L, a, b);
        UniformInt d2 = new UniformInt(123456789L, a, b);

        for (int i = 0; i < 20_000; i++) {
            int x1 = d1.sample();
            int x2 = d2.sample();
            assertTrue(x1 >= a && x1 <= b, "out of bounds: " + x1);
            assertEquals(x1, x2, "determinism failure at i=" + i);
        }
    }

    @Test
    @DisplayName("PMF, CDF, mean, variance are consistent")
    void pmfCdfMoments() {
        int a = 2, b = 6; // values 2..6 (n=5)
        UniformInt d = new UniformInt(42L, a, b);

        // pmf is flat
        for (int k = a; k <= b; k++) {
            assertEquals(1.0 / 5.0, d.pmf(k), 1e-12);
        }
        assertEquals(0.0, d.pmf(a - 1), 0.0);
        assertEquals(0.0, d.pmf(b + 1), 0.0);

        // cdf steps
        assertEquals(0.0, d.cdf(a - 1), 0.0);
        assertEquals(1.0 / 5.0, d.cdf(2), 1e-12);
        assertEquals(2.0 / 5.0, d.cdf(3), 1e-12);
        assertEquals(3.0 / 5.0, d.cdf(4), 1e-12);
        assertEquals(4.0 / 5.0, d.cdf(5), 1e-12);
        assertEquals(1.0, d.cdf(6), 0.0);
        assertEquals(1.0, d.cdf(1000), 0.0);

        // moments
        assertEquals((a + b) / 2.0, d.mean(), 0.0);
        double n = (b - a + 1);
        assertEquals((n * n - 1.0) / 12.0, d.variance(), 1e-12);
    }

    @Test
    @DisplayName("Uniformity sanity: histogram is roughly flat")
    void uniformitySanity() {
        int a = 1, b = 6; // like a die
        UniformInt d = new UniformInt(2025L, a, b);

        int trials = 120_000;
        int[] counts = new int[b - a + 1];
        for (int i = 0; i < trials; i++) {
            int x = d.sample();
            counts[x - a]++;
        }
        double expected = trials / (double) counts.length;
        double tol = expected * 0.025; // ~2.5% tolerance
        for (int c : counts) {
            assertTrue(Math.abs(c - expected) <= tol, "histogram deviation too large: " + c);
        }
    }

    @Test
    @DisplayName("Full int range edge case: MIN_VALUE..MAX_VALUE works")
    void extremeRange() {
        UniformInt d = new UniformInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < 10_000; i++) {
            int x = d.sample();
            // Any int is valid; this is a smoke test for range handling
            assertTrue(x <= Integer.MAX_VALUE);
        }
        assertTrue(d.pmf(Integer.MIN_VALUE) > 0.0);
        assertTrue(d.pmf(Integer.MAX_VALUE) > 0.0);
    }

    @Test
    @DisplayName("Support metadata is [a,b] discrete")
    void supportMetadata() {
        int a = 10, b = 15;
        UniformInt d = new UniformInt(314159L, a, b);
        var s = d.support();
        assertEquals(a, s.lower(), 0.0);
        assertEquals(b, s.upper(), 0.0);
        assertTrue(s.isLowerClosed());
        assertTrue(s.isUpperClosed());
        assertEquals(org.loudouncodes.randkit.api.DistributionSupport.Kind.DISCRETE, s.kind());
        assertTrue(s.contains(a));
        assertTrue(s.contains(b));
        assertFalse(s.contains(a - 1));
        assertFalse(s.contains(b + 1));
    }
}
