package org.loudouncodes.randkit.discrete;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.loudouncodes.randkit.api.DistributionSupport;

class NormalIntTest {

  // ---------- constructor & parameter validation ----------

  @Test
  @DisplayName("Constructor validation: sigma > 0, finite params, truncation bounds")
  void constructorValidation() {
    assertThrows(IllegalArgumentException.class, () -> new NormalInt(0.0, 0.0));
    assertThrows(IllegalArgumentException.class, () -> new NormalInt(0.0, -1.0));
    assertThrows(IllegalArgumentException.class, () -> new NormalInt(Double.NaN, 1.0));
    assertThrows(IllegalArgumentException.class, () -> new NormalInt(0.0, Double.NaN));
    assertThrows(
        IllegalArgumentException.class, () -> new NormalInt(Double.POSITIVE_INFINITY, 1.0));
    assertThrows(
        IllegalArgumentException.class, () -> new NormalInt(0.0, Double.POSITIVE_INFINITY));

    // truncated: lower must be <= upper
    assertThrows(IllegalArgumentException.class, () -> new NormalInt(0.0, 1.0, 5, 4));
  }

  // ---------- determinism & sampling bounds ----------

  @Test
  @DisplayName("Determinism by seed: same seed -> identical sequence")
  void determinismBySeed() {
    NormalInt d1 = new NormalInt(123456789L, 1.7, 2.3);
    NormalInt d2 = new NormalInt(123456789L, 1.7, 2.3);

    for (int i = 0; i < 10_000; i++) {
      assertEquals(d1.sample(), d2.sample(), "Sequences diverged at i=" + i);
    }
  }

  @Test
  @DisplayName("Truncation: samples stay within [lower, upper], CDF clamps to 0/1")
  void truncationBounds() {
    int lower = -2, upper = 3;
    NormalInt d = new NormalInt(42L, /*mean*/ 0.3, /*sigma*/ 1.1, lower, upper);

    for (int i = 0; i < 50_000; i++) {
      int x = d.sample();
      assertTrue(x >= lower && x <= upper, "out of bounds sample: " + x);
    }

    assertEquals(0.0, d.cdf(lower - 1), 0.0);
    assertEquals(1.0, d.cdf(upper), 0.0);

    var s = d.support();
    assertEquals(lower, s.lower(), 0.0);
    assertEquals(upper, s.upper(), 0.0);
    assertTrue(s.isLowerClosed());
    assertTrue(s.isUpperClosed());
    assertEquals(DistributionSupport.Kind.DISCRETE, s.kind());
  }

  // ---------- pmf/cdf properties (rounded-normal semantics) ----------

  @Test
  @DisplayName("PMF non-negative, sum to ~1 over a wide window; CDF monotone in [0,1]")
  void pmfCdfProperties() {
    double mean = 2.3, sigma = 1.7;
    NormalInt d = new NormalInt(2025L, mean, sigma);

    int a = (int) Math.floor(mean - 8.0 * sigma);
    int b = (int) Math.ceil(mean + 8.0 * sigma);

    double sum = 0.0;
    double lastCdf = 0.0;
    for (int k = a; k <= b; k++) {
      double p = d.pmf(k);
      assertTrue(p >= 0.0, "negative pmf at k=" + k);
      sum += p;

      double F = d.cdf(k);
      assertTrue(F >= 0.0 && F <= 1.0, "cdf out of [0,1] at k=" + k + ": " + F);
      assertTrue(F + 1e-15 >= lastCdf, "cdf not monotone at k=" + k);
      lastCdf = F;
    }
    // Mass outside ±8σ is negligible for a normal; allow a tiny epsilon.
    assertEquals(1.0, sum, 1e-6);

    // CDF should be near 0 far below mean, near 1 far above
    assertTrue(d.cdf(a) < 1e-6, "left tail too large");
    assertTrue(1.0 - d.cdf(b) < 1e-6, "right tail too large");

    // Support should report unbounded discrete
    var s = d.support();
    assertTrue(s.isUnboundedBelow());
    assertTrue(s.isUnboundedAbove());
    assertEquals(DistributionSupport.Kind.DISCRETE, s.kind());
  }

  @Test
  @DisplayName("Symmetry: for integer mean, pmf is (approximately) symmetric")
  void symmetryAroundIntegerMean() {
    // Integer mean; for rounded-normal, pmf should be symmetric around mean
    double mean = 0.0, sigma = 2.5;
    NormalInt d = new NormalInt(7L, mean, sigma);

    for (int offset = 0; offset <= 8; offset++) {
      double pPlus = d.pmf((int) Math.round(mean) + offset);
      double pMinus = d.pmf((int) Math.round(mean) - offset);
      assertEquals(pMinus, pPlus, 1e-12, "asymmetry at offset=" + offset);
    }
  }

  @Test
  @DisplayName("CDF/PMF consistency: CDF(k) ≈ sum_{i<=k} PMF(i)")
  void cdfMatchesPmfSum() {
    NormalInt d = new NormalInt(2024L, /*mean*/ 1.2, /*sigma*/ 1.4);

    int a = (int) Math.floor(1.2 - 8 * 1.4);
    int b = (int) Math.ceil(1.2 + 8 * 1.4);

    double running = 0.0;
    for (int k = a; k <= b; k++) {
      running += d.pmf(k);
      assertEquals(running, d.cdf(k), 1e-9, "cdf mismatch at k=" + k);
    }
    assertEquals(1.0, running, 1e-6);
  }

  // ---------- moment checks (Monte Carlo vs reported mean/variance) ----------

  @Test
  @DisplayName("Monte Carlo moments track mean() and variance()")
  void monteCarloMoments() {
    NormalInt d = new NormalInt(999L, /*mean*/ 0.75, /*sigma*/ 2.0);

    final int n = 80_000;
    long sum = 0;
    double sumsq = 0.0;
    for (int i = 0; i < n; i++) {
      int x = d.sample();
      sum += x;
      sumsq += (double) x * (double) x;
    }
    double sampleMean = sum / (double) n;
    double sampleVar = (sumsq / n) - sampleMean * sampleMean;

    // Compare to distribution-reported values with reasonable tolerances
    assertEquals(d.mean(), sampleMean, 0.08, "mean mismatch");
    assertEquals(d.variance(), sampleVar, 0.15, "variance mismatch");
  }

  // ---------- truncated distribution mass sums to 1 exactly (over bounds) ----------

  @Test
  @DisplayName("Truncated: sum of PMF over [lower, upper] is 1")
  void truncatedMassIsOne() {
    int lower = -3, upper = 4;
    NormalInt d = new NormalInt(314159L, /*mean*/ 0.4, /*sigma*/ 1.2, lower, upper);

    double sum = 0.0;
    for (int k = lower; k <= upper; k++) {
      double p = d.pmf(k);
      assertTrue(p >= 0.0, "negative pmf at k=" + k);
      sum += p;
    }
    assertEquals(1.0, sum, 1e-12);

    assertEquals(0.0, d.cdf(lower - 1), 0.0);
    assertEquals(1.0, d.cdf(upper), 0.0);
  }
}
