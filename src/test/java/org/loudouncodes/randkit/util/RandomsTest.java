package org.loudouncodes.randkit.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RandomsTest {

  @Test
  @DisplayName("defaultGenerator returns a usable RandomGenerator")
  void defaultGeneratorWorks() {
    RandomGenerator g = Randoms.defaultGenerator();
    assertNotNull(g, "defaultGenerator() returned null");
    // basic smoke: nextInt/nextDouble do not throw and have plausible ranges
    int i = g.nextInt();
    double d = g.nextDouble();
    assertTrue(d >= 0.0 && d < 1.0, "nextDouble() out of [0,1)");
    // (no assertion on i; just confirming call succeeds)
  }

  @Test
  @DisplayName("seeded: same seed -> identical sequence")
  void seededDeterminism() {
    long seed = 123456789L;
    RandomGenerator g1 = Randoms.seeded(seed);
    RandomGenerator g2 = Randoms.seeded(seed);

    for (int k = 0; k < 10_000; k++) {
      // Use a mix of methods to exercise the generator
      assertEquals(g1.nextInt(), g2.nextInt(), "nextInt mismatch at k=" + k);
      assertEquals(g1.nextLong(), g2.nextLong(), "nextLong mismatch at k=" + k);
      double d1 = g1.nextDouble();
      double d2 = g2.nextDouble();
      assertEquals(d1, d2, 0.0, "nextDouble mismatch at k=" + k);
    }
  }

  @Test
  @DisplayName("seeded: different seeds -> sequences differ")
  void differentSeedsDiffer() {
    RandomGenerator g1 = Randoms.seeded(1L);
    RandomGenerator g2 = Randoms.seeded(2L);

    boolean anyDiff = false;
    for (int k = 0; k < 1000; k++) {
      if (g1.nextLong() != g2.nextLong()) {
        anyDiff = true;
        break;
      }
    }
    assertTrue(anyDiff, "Sequences from different seeds should differ");
  }
}
