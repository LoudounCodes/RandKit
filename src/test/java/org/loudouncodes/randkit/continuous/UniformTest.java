package org.loudouncodes.randkit.continuous;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UniformDoubleTest {

    @Test
    @DisplayName("Constructor validation: a < b and finite bounds")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new UniformDouble(1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new UniformDouble(2.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new UniformDouble(Double.NEGATIVE_INFINITY, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new UniformDouble(0.0, Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("Sampling respects [a,b) bounds")
    void samplingWithinBounds() {
        double a = -3.5;
        double b = 2.25;
        UniformDouble u = new UniformDouble(12345L, a, b);

        for (int i = 0; i < 50_000; i++) {
            double x = u.sample();
            assertTrue(x >= a, "sample below lower bound");
            assertTrue(x <  b, "sample at/above upper bound");
        }
    }

    @Test
    @DisplayName("PDF and CDF formulas on [a,b)")
    void pdfCdf() {
        double a = 2.0;
        double b = 5.0;
        UniformDouble u = new UniformDouble(42L, a, b);

        // PDF
        assertEquals(0.0, u.pdf(1.999999), 0.0);
        assertEquals(1.0 / (b - a), u.pdf(3.0), 1e-12);
        assertEquals(0.0, u.pdf(5.0), 0.0); // right endpoint excluded

        // CDF
        assertEquals(0.0, u.cdf(a), 0.0);
        assertEquals(1.0, u.cdf(b), 0.0);
        assertEquals((3.0 - a) / (b - a), u.cdf(3.0), 1e-12);
    }

    @Test
    @DisplayName("Quantile endpoints and interior")
    void quantile() {
        double a = -1.0;
        double b = 3.0;
        UniformDouble u = new UniformDouble(7L, a, b);

        assertEquals(a, u.quantile(0.0), 0.0);
        assertEquals(b, u.quantile(1.0), 0.0);
        assertEquals(a + 0.25 * (b - a), u.quantile(0.25), 1e-12);
        assertEquals(a + 0.75 * (b - a), u.quantile(0.75), 1e-12);

        assertThrows(IllegalArgumentException.class, () -> u.quantile(-0.0001));
        assertThrows(IllegalArgumentException.class, () -> u.quantile(1.0001));
    }

    @Test
    @DisplayName("Mean and variance via Monte Carlo (tolerances)")
    void meanVarianceMonteCarlo() {
        double a = -2.0;
        double b = 4.0; // width = 6
        double expectedMean = 0.5 * (a + b);           // 1.0
        double expectedVar  = (Math.pow(b - a, 2)) / 12.0; // 3.0

        UniformDouble u = new UniformDouble(123456789L, a, b);

        final int n = 50_000;
        double sum = 0.0;
        double sumsq = 0.0;
        for (int i = 0; i < n; i++) {
            double x = u.sample();
            sum += x;
            sumsq += x * x;
        }
        double mean = sum / n;
        double var = (sumsq / n) - mean * mean;

        assertEquals(expectedMean, mean, 0.03);
        assertEquals(expectedVar,  var,  0.05);
    }

    @Test
    @DisplayName("Determinism: same seed -> identical sequence")
    void determinismBySeed() {
        UniformDouble u1 = new UniformDouble(999L, 0.0, 1.0);
        UniformDouble u2 = new UniformDouble(999L, 0.0, 1.0);

        for (int i = 0; i < 10_000; i++) {
            assertEquals(u1.sample(), u2.sample(), 0.0, "Sequences diverged at i=" + i);
        }
    }

    @Test
    @DisplayName("Support reports [a,b) correctly")
    void supportMetadata() {
        double a = 2.5;
        double b = 3.5;
        UniformDouble u = new UniformDouble(314159L, a, b);

        var supp = u.support();
        assertEquals(a, supp.lower(), 0.0);
        assertEquals(b, supp.upper(), 0.0);
        assertTrue(supp.isLowerClosed());
        assertFalse(supp.isUpperClosed());
        assertTrue(supp.contains(a));
        assertTrue(supp.contains((a + b) / 2.0));
        assertFalse(supp.contains(b)); // half-open on the right
    }
}
