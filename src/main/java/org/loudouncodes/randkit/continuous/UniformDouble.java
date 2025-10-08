package org.loudouncodes.randkit.continuous;

import java.util.random.RandomGenerator;
import org.loudouncodes.randkit.api.ContinuousDistribution;
import org.loudouncodes.randkit.api.DistributionSupport;
import org.loudouncodes.randkit.util.Randoms;

/**
 * Continuous Uniform(a, b) on the half-open interval {@code [a, b)} with {@code a < b}.
 *
 * <p>PDF: {@code f(x) = 1 / (b - a)} for {@code x ∈ [a, b)}, else 0.<br>
 * CDF: {@code F(x) = 0} for {@code x ≤ a}; {@code (x - a)/(b - a)} for {@code a < x < b}; and
 * {@code 1} for {@code x ≥ b}.
 */
public final class UniformDouble implements ContinuousDistribution {

  private final RandomGenerator rng;
  private final double a;
  private final double b;
  private final double width;

  /**
   * Constructs a UniformDouble(a, b) using a platform-provided, high-quality default RNG.
   *
   * @param a inclusive lower bound
   * @param b exclusive upper bound; must satisfy {@code a < b}
   * @throws IllegalArgumentException if {@code a >= b} or any bound is not finite
   */
  public UniformDouble(double a, double b) {
    this(Randoms.defaultGenerator(), a, b);
  }

  /**
   * Constructs a UniformDouble(a, b) with a deterministic seed.
   *
   * @param seed random seed for reproducibility
   * @param a inclusive lower bound
   * @param b exclusive upper bound; must satisfy {@code a < b}
   * @throws IllegalArgumentException if {@code a >= b} or any bound is not finite
   */
  public UniformDouble(long seed, double a, double b) {
    this(Randoms.seeded(seed), a, b);
  }

  /**
   * Constructs a UniformDouble(a, b) using the provided generator.
   *
   * @param rng non-null random generator supplying independent U(0,1) variates
   * @param a inclusive lower bound
   * @param b exclusive upper bound; must satisfy {@code a < b}
   * @throws NullPointerException if {@code rng} is null
   * @throws IllegalArgumentException if {@code a >= b} or any bound is not finite
   */
  public UniformDouble(RandomGenerator rng, double a, double b) {
    if (rng == null) throw new NullPointerException("rng must not be null");
    if (!Double.isFinite(a) || !Double.isFinite(b)) {
      throw new IllegalArgumentException("Bounds must be finite");
    }
    if (!(a < b)) {
      throw new IllegalArgumentException("Require a < b (got a=" + a + ", b=" + b + ")");
    }
    this.rng = rng;
    this.a = a;
    this.b = b;
    this.width = b - a;
  }

  /** {@inheritDoc} */
  @Override
  public double sample() {
    return a + rng.nextDouble() * width; // maps U[0,1) -> [a,b)
  }

  /** {@inheritDoc} */
  @Override
  public double pdf(double x) {
    return (x >= a && x < b) ? (1.0 / width) : 0.0;
  }

  /** {@inheritDoc} */
  @Override
  public double cdf(double x) {
    if (x <= a) return 0.0;
    if (x >= b) return 1.0;
    return (x - a) / width;
  }

  /** {@inheritDoc} */
  @Override
  public double quantile(double p) {
    if (!(p >= 0.0 && p <= 1.0)) {
      throw new IllegalArgumentException("p must be in [0,1], got " + p);
    }
    if (p == 0.0) return a;
    if (p == 1.0) return b; // for [a,b) we return b at p=1 by convention
    return a + p * width;
  }

  /** {@inheritDoc} */
  @Override
  public double mean() {
    return 0.5 * (a + b);
  }

  /** {@inheritDoc} */
  @Override
  public double variance() {
    return (width * width) / 12.0;
  }

  /**
   * Returns the mathematical support of this distribution: the half-open interval [a, b).
   *
   * @return a {@link DistributionSupport} describing {@code [a, b)}
   */
  public DistributionSupport support() {
    return DistributionSupport.continuous(a, /* lowerClosed= */ true, b, /* upperClosed= */ false);
  }

  /**
   * The inclusive lower bound {@code a}.
   *
   * @return the lower bound value {@code a}
   */
  public double lowerBound() {
    return a;
  }

  /**
   * The exclusive upper bound {@code b}.
   *
   * @return the upper bound value {@code b}
   */
  public double upperBound() {
    return b;
  }
}
