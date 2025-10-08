package org.loudouncodes.randkit.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DistributionSupportTest {

  @Test
  @DisplayName("Continuous factory builds expected bounds and openness/closedness")
  void continuousFactory() {
    DistributionSupport s = DistributionSupport.continuous(-2.0, true, 3.5, false);

    assertEquals(-2.0, s.lower(), 0.0);
    assertEquals(3.5, s.upper(), 0.0);
    assertTrue(s.isLowerClosed());
    assertFalse(s.isUpperClosed());
    assertEquals(DistributionSupport.Kind.CONTINUOUS, s.kind());

    // Contains logic respects half-open interval
    assertTrue(s.contains(-2.0));
    assertTrue(s.contains(0.0));
    assertFalse(s.contains(3.5));
    assertFalse(s.contains(-2.0000001));
  }

  @Test
  @DisplayName("Discrete factory builds expected metadata")
  void discreteFactory() {
    DistributionSupport s = DistributionSupport.discrete(0.0, true, 10.0, true);

    assertEquals(0.0, s.lower(), 0.0);
    assertEquals(10.0, s.upper(), 0.0);
    assertTrue(s.isLowerClosed());
    assertTrue(s.isUpperClosed());
    assertEquals(DistributionSupport.Kind.DISCRETE, s.kind());

    // Numeric interval check only (doesn't enforce integer-ness)
    assertTrue(s.contains(0.0));
    assertTrue(s.contains(10.0));
    assertTrue(s.contains(5.5));
    assertFalse(s.contains(-0.0001));
    assertFalse(s.contains(10.0001));
  }

  @Test
  @DisplayName("Unbounded presets behave as advertised")
  void presets() {
    DistributionSupport real = DistributionSupport.REAL_LINE;
    assertTrue(real.isUnboundedBelow());
    assertTrue(real.isUnboundedAbove());
    assertEquals(DistributionSupport.Kind.CONTINUOUS, real.kind());
    assertTrue(real.contains(0.0));
    assertTrue(real.contains(-123.456));
    assertTrue(real.contains(7.89e10));

    DistributionSupport nonNeg = DistributionSupport.NON_NEGATIVE_REALS;
    assertTrue(nonNeg.isLowerClosed());
    assertFalse(nonNeg.isUpperClosed());
    assertTrue(nonNeg.contains(0.0));
    assertTrue(nonNeg.contains(1.0));
    assertFalse(nonNeg.contains(-1e-12));

    DistributionSupport unitOpen = DistributionSupport.UNIT_INTERVAL_OPEN;
    assertFalse(unitOpen.isLowerClosed());
    assertFalse(unitOpen.isUpperClosed());
    assertFalse(unitOpen.contains(0.0));
    assertFalse(unitOpen.contains(1.0));
    assertTrue(unitOpen.contains(0.5));

    DistributionSupport unitClosed = DistributionSupport.UNIT_INTERVAL_CLOSED;
    assertTrue(unitClosed.contains(0.0));
    assertTrue(unitClosed.contains(1.0));

    DistributionSupport nonNegInts = DistributionSupport.NON_NEGATIVE_INTEGERS;
    assertEquals(DistributionSupport.Kind.DISCRETE, nonNegInts.kind());
    assertTrue(nonNegInts.contains(0.0));
    assertFalse(nonNegInts.contains(-1.0));
  }

  @Test
  @DisplayName("Invalid finite bounds are rejected")
  void invalidBounds() {
    assertThrows(
        IllegalArgumentException.class, () -> DistributionSupport.continuous(5.0, true, 5.0, true));
    assertThrows(
        IllegalArgumentException.class,
        () -> DistributionSupport.continuous(6.0, false, 5.0, true));
    assertThrows(
        IllegalArgumentException.class, () -> DistributionSupport.discrete(3.0, true, 2.0, true));
  }

  @Test
  @DisplayName("equals and hashCode reflect all fields")
  void equalsHashCode() {
    DistributionSupport a = DistributionSupport.continuous(0.0, true, 1.0, false);
    DistributionSupport b = DistributionSupport.continuous(0.0, true, 1.0, false);
    DistributionSupport c = DistributionSupport.discrete(0.0, true, 1.0, false);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a.hashCode(), c.hashCode());
  }

  @Test
  @DisplayName("toString provides a readable summary (non-empty)")
  void toStringSmoke() {
    String s = DistributionSupport.NON_NEGATIVE_REALS.toString();
    assertNotNull(s);
    assertFalse(s.isBlank());
  }
}
