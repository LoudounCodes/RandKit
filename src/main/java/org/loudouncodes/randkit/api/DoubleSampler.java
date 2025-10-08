package org.loudouncodes.randkit.api;

/**
 * Minimal contract for components that can produce double-valued random samples. Useful for
 * simulations and utilities that only require sampling, not full distribution methods such as pdf,
 * cdf, or quantiles.
 */
@FunctionalInterface
public interface DoubleSampler {
  /**
   * Produce one random sample.
   *
   * @return a sampled value
   */
  double sample();
}
