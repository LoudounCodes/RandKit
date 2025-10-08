package org.loudouncodes.randkit.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SamplerInterfacesTest {

  @Test
  @DisplayName("DoubleSampler is a functional interface usable with lambdas")
  void doubleSamplerLambda() {
    DoubleSampler constant = () -> 1.2345;
    assertEquals(1.2345, constant.sample(), 0.0);

    // A trivial composition example: average of N samples
    DoubleSampler three = () -> 3.0;
    double sum = 0.0;
    for (int i = 0; i < 10; i++) sum += three.sample();
    assertEquals(30.0, sum, 0.0);
  }

  @Test
  @DisplayName("IntSampler is a functional interface usable with lambdas")
  void intSamplerLambda() {
    IntSampler die = () -> 4; // the only winning move 😉
    for (int i = 0; i < 5; i++) {
      assertEquals(4, die.sample());
    }
  }
}
