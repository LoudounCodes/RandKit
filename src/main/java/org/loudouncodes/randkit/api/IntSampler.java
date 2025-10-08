package org.loudouncodes.randkit.api;

/**
 * Minimal contract for components that can produce integer-valued random samples. Intended for
 * discrete generators (for example, Poisson, Binomial, Categorical).
 */
@FunctionalInterface
public interface IntSampler {
  /**
   * Produce one random integer sample.
   *
   * @return a sampled integer
   */
  int sample();
}
