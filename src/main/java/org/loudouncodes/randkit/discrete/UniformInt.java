package org.loudouncodes.randkit.discrete;

import java.util.random.RandomGenerator;
import org.loudouncodes.randkit.api.DiscreteDistribution;
import org.loudouncodes.randkit.api.DistributionSupport;
import org.loudouncodes.randkit.util.Randoms;

/**
 * Discrete Uniform distribution on the closed integer interval {@code [a, b]} with {@code a ≤ b}.
 *
 * <p>PMF: {@code P(X = k) = 1 / (b - a + 1)} for {@code k ∈ [a, b]}, else 0.<br>
 * CDF: {@code F(k) = 0} for {@code k < a}; {@code (k - a + 1)/(b - a + 1)} for {@code a ≤ k ≤ b};
 * and {@code 1} for {@code k > b}.<br>
 * Mean: {@code (a + b) / 2}. Variance: {@code ((n^2 - 1) / 12)}, where {@code n = b - a + 1}.
 */
public final class UniformInt implements DiscreteDistribution {

  private final RandomGenerator rng;
  private final int a;
  private final int b;
  private final long n; // cardinality = (b - a + 1) as long to avoid overflow
  private final double pmf; // 1.0 / n, cached
  private final double mean;
  private final double variance;

  /**
   * Constructs a UniformInt(a, b) using a platform-provided, high-quality default RNG.
   *
   * @param a inclusive lower bound
   * @param b inclusive upper bound; must satisfy {@code a ≤ b}
   * @throws IllegalArgumentException if {@code a > b}
   */
  public UniformInt(int a, int b) {
    this(Randoms.defaultGenerator(), a, b);
  }

  /**
   * Constructs a UniformInt(a, b) with a deterministic seed.
   *
   * @param seed random seed for reproducibility
   * @param a inclusive lower bound
   * @param b inclusive upper bound; must satisfy {@code a ≤ b}
   * @throws IllegalArgumentException if {@code a > b}
   */
  public UniformInt(long seed, int a, int b) {
    this(Randoms.seeded(seed), a, b);
  }

  /**
   * Constructs a UniformInt(a, b) using the provided generator.
   *
   * @param rng non-null random generator
   * @param a inclusive lower bound
   * @param b inclusive upper bound; must satisfy {@code a ≤ b}
   * @throws NullPointerException if {@code rng} is null
   * @throws IllegalArgumentException if {@code a > b}
   */
  public UniformInt(RandomGenerator rng, int a, int b) {
    if (rng == null) throw new NullPointerException("rng must not be null");
    if (a > b) throw new IllegalArgumentException("Require a ≤ b (got a=" + a + ", b=" + b + ")");
    this.rng = rng;
    this.a = a;
    this.b = b;

    // cardinality in long to avoid overflow when b - a == Integer.MAX_VALUE
    this.n = ((long) b - (long) a) + 1L;

    this.pmf = 1.0 / (double) n;
    this.mean = 0.5 * ((double) a + (double) b);

    // variance = (n^2 - 1) / 12 for discrete uniform on [a,b]
    double nn = (double) n;
    this.variance = (nn * nn - 1.0) / 12.0;
  }

  /** {@inheritDoc} */
  @Override
  public int sample() {
    long r = rng.nextLong(n); // unbiased [0, n)
    long val = ((long) a) + r; // shift to [a, b]
    return (int) val;
  }

  /** {@inheritDoc} */
  @Override
  public double pmf(int k) {
    return (k >= a && k <= b) ? pmf : 0.0;
  }

  /** {@inheritDoc} */
  @Override
  public double cdf(int k) {
    if (k < a) return 0.0;
    if (k >= b) return 1.0;
    long count = ((long) k - (long) a) + 1L;
    return ((double) count) / ((double) n);
  }

  /** {@inheritDoc} */
  @Override
  public double mean() {
    return mean;
  }

  /** {@inheritDoc} */
  @Override
  public double variance() {
    return variance;
  }

  /**
   * Returns the mathematical support of this distribution: the closed interval [a, b] (discrete).
   *
   * @return a {@link DistributionSupport} describing {@code [a, b]}
   */
  public DistributionSupport support() {
    return DistributionSupport.discrete(a, /* lowerClosed= */ true, b, /* upperClosed= */ true);
  }

  /**
   * The inclusive lower bound {@code a}.
   *
   * @return the lower bound value {@code a}
   */
  public int lowerBound() {
    return a;
  }

  /**
   * The inclusive upper bound {@code b}.
   *
   * @return the upper bound value {@code b}
   */
  public int upperBound() {
    return b;
  }
}
