package org.loudouncodes.randkit.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract checks for the ContinuousDistribution interface using a simple Uniform(a,b)
 * implementation defined locally for tests.
 */
class ContinuousDistributionTest {

  /** Minimal Uniform(a,b) for testing the interface (not part of production code). */
  private static final class TestUniform implements ContinuousDistribution {
    private final double a, b, width;
    private final RandomGenerator rng;

    TestUniform(double a, double b) {
      if (!Double.isFinite(a) || !Double.isFinite(b) || !(a < b)) {
        throw new IllegalArgumentException("Require finite a < b");
      }
      this.a = a;
      this.b = b;
      this.width = b - a;
      this.rng = RandomGeneratorFactory.of("L64X128MixRandom").create(1234L);
    }

    @Override
    public double sample() {
      return a + rng.nextDouble() * width;
    }

    @Override
    public double pdf(double x) {
      return (x >= a && x < b) ? (1.0 / width) : 0.0;
    }

    @Override
    public double cdf(double x) {
      if (x <= a) return 0.0;
      if (x >= b) return 1.0;
      return (x - a) / width;
    }

    @Override
    public double quantile(double p) {
      if (!(p >= 0.0 && p <= 1.0)) throw new IllegalArgumentException("p in [0,1] required");
      if (p == 0.0) return a;
      if (p == 1.0) return b; // adopt [a,b] for endpoint convention in this test impl
      return a + p * width;
    }

    @Override
    public double mean() {
      return 0.5 * (a + b);
    }

    @Override
    public double variance() {
      return (width * width) / 12.0;
    }
  }

  @Test
  @DisplayName("Constructor validation: finite a < b")
  void constructorValidation() {
    assertThrows(IllegalArgumentException.class, () -> new TestUniform(1.0, 1.0));
    assertThrows(IllegalArgumentException.class, () -> new TestUniform(2.0, 1.0));
    assertThrows(
        IllegalArgumentException.class, () -> new TestUniform(Double.NEGATIVE_INFINITY, 0.0));
    assertThrows(
        IllegalArgumentException.class, () -> new TestUniform(0.0, Double.POSITIVE_INFINITY));
  }

  @Test
  @DisplayName("PDF and CDF follow Uniform(a,b) formulas")
  void pdfCdfFormulas() {
    var u = new TestUniform(-2.0, 3.0); // width = 5
    assertEquals(0.0, u.pdf(-2.0001), 0.0);
    assertEquals(1.0 / 5.0, u.pdf(0.0), 1e-12);
    assertEquals(0.0, u.pdf(3.0), 0.0); // right endpoint excluded for pdf

    assertEquals(0.0, u.cdf(-2.0), 0.0);
    assertEquals(1.0, u.cdf(3.0), 0.0);
    assertEquals((1.0 - (-2.0)) / 5.0, u.cdf(1.0), 1e-12);
  }

  @Test
  @DisplayName("CDF is monotone in [0,1]")
  void cdfMonotoneBounded() {
    var u = new TestUniform(0.5, 1.5);
    double prev = -1.0;
    for (double x = -1.0; x <= 2.0; x += 0.05) {
      double F = u.cdf(x);
      assertTrue(F >= 0.0 && F <= 1.0, "CDF out of bounds at x=" + x);
      assertTrue(F >= prev, "CDF not monotone at x=" + x);
      prev = F;
    }
  }

  @Test
  @DisplayName("Quantile endpoints, interior, and inverse relation to CDF")
  void quantileBehavior() {
    var u = new TestUniform(-1.0, 3.0);
    assertEquals(-1.0, u.quantile(0.0), 0.0);
    assertEquals(3.0, u.quantile(1.0), 0.0);
    assertEquals(0.0, u.quantile(0.25), 1e-12);
    assertEquals(2.0, u.quantile(0.75), 1e-12);

    assertThrows(IllegalArgumentException.class, () -> u.quantile(-0.01));
    assertThrows(IllegalArgumentException.class, () -> u.quantile(1.01));

    // Approximate inverse check: F(Q(p)) ≈ p for interior p
    double[] ps = {0.1, 0.25, 0.5, 0.75, 0.9};
    for (double p : ps) {
      double q = u.quantile(p);
      double F = u.cdf(q);
      assertEquals(p, F, 1e-12);
    }
  }

  @Test
  @DisplayName("Mean and variance match analytic values (Monte Carlo sanity)")
  void meanVarianceMonteCarlo() {
    var u = new TestUniform(-2.0, 4.0); // width = 6, mean = 1, var = 3
    final int n = 60_000;
    double sum = 0.0, sumsq = 0.0;
    for (int i = 0; i < n; i++) {
      double x = u.sample();
      sum += x;
      sumsq += x * x;
    }
    double mean = sum / n;
    double var = (sumsq / n) - mean * mean;

    assertEquals(1.0, mean, 0.03); // ~3e-2 tolerance
    assertEquals(3.0, var, 0.06); // ~6e-2 tolerance
  }
}
