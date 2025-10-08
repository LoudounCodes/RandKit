package org.loudouncodes.randkit.discrete;

import java.util.random.RandomGenerator;
import org.loudouncodes.randkit.api.DiscreteDistribution;
import org.loudouncodes.randkit.api.DistributionSupport;
import org.loudouncodes.randkit.util.Randoms;

/**
 * A <em>discrete normal</em> (rounded-normal) distribution on the integers.
 *
 * <p>This distribution is constructed by sampling a continuous normal {@code X ~ Normal(mean,
 * sigma^2)} and returning the integer {@code Y = round(X)} (nearest-even rounding via {@link
 * Math#rint(double)}). It therefore places probability mass on all integers (unless truncated).
 *
 * <h2>Probability functions</h2>
 *
 * <p>For the <strong>untruncated</strong> rounded-normal, the probability mass function (PMF) and
 * cumulative distribution function (CDF) are exact:
 *
 * <pre>{@code
 * pmf(k) = Φ((k + 0.5 - mean)/sigma) - Φ((k - 0.5 - mean)/sigma)
 * cdf(k) = Φ((k + 0.5 - mean)/sigma)
 * }</pre>
 *
 * where {@code Φ} is the standard normal CDF.
 *
 * <p>For the <strong>truncated</strong> variant on {@code [lower, upper]} (inclusive), mass is
 * re-normalized to that interval:
 *
 * <pre>{@code
 * Z = Φ((upper + 0.5 - mean)/sigma) - Φ((lower - 0.5 - mean)/sigma)
 * pmf_trunc(k) = pmf(k) / Z     for k in [lower, upper], else 0
 * cdf_trunc(k) = (Φ((k + 0.5 - mean)/sigma) - Φ((lower - 0.5 - mean)/sigma)) / Z
 * }</pre>
 *
 * <h2>Sampling</h2>
 *
 * <p>Sampling uses the Marsaglia polar (Box–Muller) method to draw a standard normal, then
 * scales/shifts and applies nearest-even rounding. When truncated, out-of-range draws are rejected
 * and re-sampled (efficient unless the window is deep in the tails).
 *
 * <h2>Numerics</h2>
 *
 * <ul>
 *   <li>{@code Φ} is computed via an {@code erf} approximation (Abramowitz &amp; Stegun 7.1.26),
 *       with max absolute error ≈ 1.5e−7.
 *   <li>Untruncated mean/variance are computed by summing the PMF over a wide {@code ±8σ} window
 *       (extended until the remaining tail mass is negligible).
 * </ul>
 *
 * <h2>Determinism &amp; threading</h2>
 *
 * <p>Given the same seed, parameters, and JDK RNG algorithm, sequences are repeatable. Instances
 * are not synchronized; prefer one instance per thread or supply thread-local RNGs.
 *
 * <h2>Examples</h2>
 *
 * <pre>{@code
 * // Untruncated discrete normal centered near 2, σ = 1.5
 * var d1 = new NormalInt(2.0, 1.5);
 * int x = d1.sample();
 *
 * // Seeded and truncated to [-3, +3]
 * var d2 = new NormalInt(1234L, 0.0, 2.0, -3, 3);
 * double p0 = d2.pmf(0);      // probability at 0
 * double F2 = d2.cdf(2);      // P(Y <= 2)
 * }</pre>
 *
 * @since 0.1.0
 */
public final class NormalInt implements DiscreteDistribution {

  /** RNG supplying uniform bits; provided or created via {@link Randoms}. */
  private final RandomGenerator rng;

  /** Mean {@code μ} of the underlying continuous normal. */
  private final double meanParam;

  /** Standard deviation {@code σ} of the underlying continuous normal (must be {@code > 0}). */
  private final double sigma;

  // Truncation metadata (active when 'truncated' is true).
  private final boolean truncated;
  private final int lower;
  private final int upper;
  private final double normZ; // normalization constant for truncated mass
  private final boolean degenerate; // when truncation window has ~zero mass
  private final int degenerateValue;

  // Cached moments reported by the API.
  private final double meanCached;
  private final double varianceCached;

  // Box–Muller (polar) spare sample caching.
  private boolean haveSpare = false;
  private double spare = 0.0;

  private static final double INV_SQRT2 = 1.0 / Math.sqrt(2.0);

  // ---------- Constructors (untruncated) ----------

  /**
   * Creates an untruncated discrete normal using the library's default RNG.
   *
   * @param mean the mean {@code μ} of the underlying continuous normal (finite)
   * @param sigma the standard deviation {@code σ} of the underlying normal; must be {@code > 0}
   * @throws IllegalArgumentException if {@code mean} is not finite or {@code sigma <= 0} or not
   *     finite
   */
  public NormalInt(double mean, double sigma) {
    this(Randoms.defaultGenerator(), mean, sigma);
  }

  /**
   * Creates an untruncated discrete normal with a deterministic RNG built from {@code seed}.
   *
   * @param seed RNG seed for reproducible sampling
   * @param mean the mean {@code μ} of the underlying continuous normal (finite)
   * @param sigma the standard deviation {@code σ} of the underlying normal; must be {@code > 0}
   * @throws IllegalArgumentException if {@code mean} is not finite or {@code sigma <= 0} or not
   *     finite
   */
  public NormalInt(long seed, double mean, double sigma) {
    this(Randoms.seeded(seed), mean, sigma);
  }

  /**
   * Creates an untruncated discrete normal with a caller-supplied RNG.
   *
   * @param rng the random generator (must not be {@code null})
   * @param mean the mean {@code μ} of the underlying continuous normal (finite)
   * @param sigma the standard deviation {@code σ} of the underlying normal; must be {@code > 0}
   * @throws NullPointerException if {@code rng} is {@code null}
   * @throws IllegalArgumentException if {@code mean} is not finite or {@code sigma <= 0} or not
   *     finite
   */
  public NormalInt(RandomGenerator rng, double mean, double sigma) {
    validateParams(mean, sigma);
    if (rng == null) throw new NullPointerException("rng must not be null");

    this.rng = rng;
    this.meanParam = mean;
    this.sigma = sigma;

    this.truncated = false;
    this.lower = Integer.MIN_VALUE;
    this.upper = Integer.MAX_VALUE;
    this.normZ = 1.0;
    this.degenerate = false;
    this.degenerateValue = (int) Math.rint(meanParam);

    double[] m = computeUntruncatedMoments();
    this.meanCached = m[0];
    this.varianceCached = m[1];
  }

  // ---------- Constructors (truncated) ----------

  /**
   * Creates a truncated discrete normal on the closed interval {@code [lower, upper]} using the
   * library's default RNG. Mass is re-normalized to the interval.
   *
   * @param mean the mean {@code μ} of the underlying continuous normal (finite)
   * @param sigma the standard deviation {@code σ} of the underlying normal; must be {@code > 0}
   * @param lower inclusive lower bound for the integer outcomes
   * @param upper inclusive upper bound for the integer outcomes; must be {@code >= lower}
   * @throws IllegalArgumentException if parameters are invalid or {@code lower > upper}
   */
  public NormalInt(double mean, double sigma, int lower, int upper) {
    this(Randoms.defaultGenerator(), mean, sigma, lower, upper);
  }

  /**
   * Creates a truncated discrete normal on {@code [lower, upper]} with a deterministic RNG.
   *
   * @param seed RNG seed for reproducible sampling
   * @param mean the mean {@code μ} of the underlying continuous normal (finite)
   * @param sigma the standard deviation {@code σ} of the underlying normal; must be {@code > 0}
   * @param lower inclusive lower bound for the integer outcomes
   * @param upper inclusive upper bound for the integer outcomes; must be {@code >= lower}
   * @throws IllegalArgumentException if parameters are invalid or {@code lower > upper}
   */
  public NormalInt(long seed, double mean, double sigma, int lower, int upper) {
    this(Randoms.seeded(seed), mean, sigma, lower, upper);
  }

  /**
   * Creates a truncated discrete normal on {@code [lower, upper]} with a caller-supplied RNG.
   *
   * @param rng the random generator (must not be {@code null})
   * @param mean the mean {@code μ} of the underlying continuous normal (finite)
   * @param sigma the standard deviation {@code σ} of the underlying normal; must be {@code > 0}
   * @param lower inclusive lower bound for the integer outcomes
   * @param upper inclusive upper bound for the integer outcomes; must be {@code >= lower}
   * @throws NullPointerException if {@code rng} is {@code null}
   * @throws IllegalArgumentException if parameters are invalid or {@code lower > upper}
   */
  public NormalInt(RandomGenerator rng, double mean, double sigma, int lower, int upper) {
    validateParams(mean, sigma);
    if (rng == null) throw new NullPointerException("rng must not be null");
    if (lower > upper) {
      throw new IllegalArgumentException(
          "lower must be <= upper (got " + lower + " > " + upper + ")");
    }

    this.rng = rng;
    this.meanParam = mean;
    this.sigma = sigma;
    this.truncated = true;
    this.lower = lower;
    this.upper = upper;

    // Normalization mass over the truncated support:
    // Z = P(Y in [lower, upper]) = Φ(upper+0.5) - Φ(lower-0.5)
    double zUpper = normalCdf(upper + 0.5);
    double zLower = normalCdf(lower - 0.5);
    double z = zUpper - zLower;
    this.normZ = z;

    if (!(z > 0.0)) {
      // Window captures (numerically) no mass: become a point-mass at the
      // rounded mean, clamped to [lower, upper].
      this.degenerate = true;
      int k = (int) Math.rint(meanParam);
      if (k < lower) k = lower;
      if (k > upper) k = upper;
      this.degenerateValue = k;
      this.meanCached = k;
      this.varianceCached = 0.0;
    } else {
      this.degenerate = false;
      this.degenerateValue = (int) Math.rint(meanParam);
      double[] mv = computeTruncatedMoments(lower, upper, z);
      this.meanCached = mv[0];
      this.varianceCached = mv[1];
    }
  }

  // ---------- API (DiscreteDistribution) ----------

  /**
   * Draws a single sample {@code Y} from this distribution.
   *
   * <p>For untruncated distributions, this value can be any integer (with quickly decaying tails).
   * For truncated distributions, the result is guaranteed to lie in {@code [lower, upper]}.
   *
   * @return a random integer variate
   */
  @Override
  public int sample() {
    if (degenerate) return degenerateValue;

    while (true) {
      // Draw a Normal(mean, sigma^2)
      double g = meanParam + sigma * nextGaussian();
      int y = (int) Math.rint(g); // nearest-even rounding
      if (!truncated || (y >= lower && y <= upper)) {
        return y;
      }
      // else reject and resample
    }
  }

  /**
   * Returns the probability mass at {@code k}, i.e., {@code P(Y = k)}.
   *
   * @param k integer at which to evaluate the PMF
   * @return the probability mass at {@code k} (zero if truncated and {@code k} is outside the
   *     bounds)
   */
  @Override
  public double pmf(int k) {
    if (degenerate) {
      return (k == degenerateValue) ? 1.0 : 0.0;
    }
    if (truncated && (k < lower || k > upper)) return 0.0;

    double p = pmfUntruncated(k);
    return truncated ? p / normZ : p;
  }

  /**
   * Returns the cumulative distribution function at {@code k}, i.e., {@code P(Y ≤ k)}.
   *
   * @param k integer at which to evaluate the CDF
   * @return the cumulative probability at {@code k}
   */
  @Override
  public double cdf(int k) {
    if (degenerate) {
      return (k < degenerateValue) ? 0.0 : 1.0;
    }
    if (!truncated) return cdfUntruncated(k);

    if (k < lower - 1) return 0.0;
    if (k >= upper) return 1.0;

    // For truncated: (Φ(k+0.5) - Φ(lower-0.5)) / Z
    double num = normalCdf(k + 0.5) - normalCdf(lower - 0.5);
    return num / normZ;
  }

  /**
   * Returns the distribution mean (expected value).
   *
   * <p>For the rounded-normal, this is close to {@code mean}, with a tiny quantization effect. For
   * the truncated variant, this is the re-normalized mean over the interval.
   *
   * @return {@code E[Y]}
   */
  @Override
  public double mean() {
    return meanCached;
  }

  /**
   * Returns the distribution variance.
   *
   * <p>For the rounded-normal, this is close to {@code sigma^2} plus a small quantization effect.
   * For the truncated variant, this is the re-normalized variance over the interval.
   *
   * @return {@code Var[Y]}
   */
  @Override
  public double variance() {
    return varianceCached;
  }

  /**
   * Reports the mathematical support (domain) of this distribution.
   *
   * <p>For untruncated instances the support is the unbounded integer line. For truncated instances
   * it is the closed interval {@code [lower, upper]}.
   *
   * @return a {@link DistributionSupport} describing the support (discrete)
   */
  public DistributionSupport support() {
    if (!truncated) {
      return DistributionSupport.discrete(
          Double.NEGATIVE_INFINITY, false,
          Double.POSITIVE_INFINITY, false);
    }
    return DistributionSupport.discrete(lower, true, upper, true);
  }

  // ---------- Internal helpers ----------

  /**
   * Validates constructor parameters.
   *
   * @param mean the mean parameter (finite)
   * @param sigma the standard deviation parameter (finite and {@code > 0})
   * @throws IllegalArgumentException if parameters are invalid
   */
  private static void validateParams(double mean, double sigma) {
    if (!Double.isFinite(mean)) throw new IllegalArgumentException("mean must be finite");
    if (!Double.isFinite(sigma) || !(sigma > 0.0)) {
      throw new IllegalArgumentException("sigma must be finite and > 0");
    }
  }

  /**
   * PMF for the <em>untruncated</em> rounded-normal at integer {@code k}.
   *
   * @param k integer support point
   * @return {@code P(Y = k)} before any truncation is applied
   */
  private double pmfUntruncated(int k) {
    double a = normalCdf(k + 0.5);
    double b = normalCdf(k - 0.5);
    double p = a - b;
    return (p <= 0.0) ? 0.0 : p; // guard against tiny negatives from roundoff
  }

  /**
   * CDF for the <em>untruncated</em> rounded-normal at integer {@code k}.
   *
   * @param k integer support point
   * @return {@code P(Y ≤ k)} before any truncation is applied
   */
  private double cdfUntruncated(int k) {
    return normalCdf(k + 0.5);
  }

  /**
   * Standard normal CDF {@code Φ(x)} using an {@code erf} approximation (no dependency on {@code
   * Math.erf} for broad JDK compatibility).
   *
   * @param x standardized value
   * @return {@code Φ(x)}
   */
  private double normalCdf(double x) {
    double z = (x - meanParam) / sigma;
    return 0.5 * (1.0 + erfApprox(z * INV_SQRT2));
  }

  /**
   * Approximation to {@code erf(x)} with max absolute error ≈ 1.5e−7.
   *
   * <p>From Abramowitz &amp; Stegun 7.1.26 (Horner form).
   *
   * @param x input
   * @return approximate {@code erf(x)}
   */
  private static double erfApprox(double x) {
    double sign = Math.signum(x);
    double ax = Math.abs(x);
    double t = 1.0 / (1.0 + 0.3275911 * ax);
    double y =
        1.0
            - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t
                    + 0.254829592)
                * t
                * Math.exp(-ax * ax);
    return sign * y;
  }

  /**
   * Computes mean and variance for the <em>untruncated</em> rounded-normal by summing a wide window
   * of integer support (starting at {@code ±8σ} and expanding if tiny tail mass remains).
   *
   * @return array {@code [mean, variance]}
   */
  private double[] computeUntruncatedMoments() {
    int a = (int) Math.floor(meanParam - 8.0 * sigma);
    int b = (int) Math.ceil(meanParam + 8.0 * sigma);

    double mass = 0.0, m1 = 0.0, m2 = 0.0;

    for (int k = a; k <= b; k++) {
      double p = pmfUntruncated(k);
      mass += p;
      m1 += p * k;
      m2 += p * (double) k * (double) k;
    }

    int extend = 0;
    while (mass < 1.0 - 1e-12 && extend < 32) {
      a -= 1;
      b += 1;
      double pLeft = pmfUntruncated(a);
      double pRight = pmfUntruncated(b);
      mass += pLeft + pRight;
      m1 += pLeft * a + pRight * b;
      m2 += pLeft * (double) a * (double) a + pRight * (double) b * (double) b;
      extend++;
    }

    if (mass > 0.0 && Math.abs(1.0 - mass) > 1e-15) {
      m1 /= mass;
      m2 /= mass;
    }
    double var = m2 - m1 * m1;
    if (var < 0.0) var = 0.0; // numeric guard
    return new double[] {m1, var};
  }

  /**
   * Computes mean and variance for the <em>truncated</em> rounded-normal by summing exactly over
   * {@code k = lower..upper} with the renormalization constant {@code Z}.
   *
   * @param lower inclusive lower bound
   * @param upper inclusive upper bound
   * @param z renormalization constant {@code Z = P(lower ≤ Y ≤ upper)}
   * @return array {@code [mean, variance]}
   */
  private double[] computeTruncatedMoments(int lower, int upper, double z) {
    double m1 = 0.0, m2 = 0.0;
    for (int k = lower; k <= upper; k++) {
      double p = pmfUntruncated(k) / z;
      m1 += p * k;
      m2 += p * (double) k * (double) k;
    }
    double var = m2 - m1 * m1;
    if (var < 0.0) var = 0.0;
    return new double[] {m1, var};
  }

  /**
   * Draws a standard {@code Normal(0,1)} using the Marsaglia polar method, with a cached "spare"
   * sample so every other call is essentially free.
   *
   * @return a standard normal sample
   */
  private double nextGaussian() {
    if (haveSpare) {
      haveSpare = false;
      return spare;
    }
    double u, v, s;
    do {
      u = 2.0 * rng.nextDouble() - 1.0; // (-1,1)
      v = 2.0 * rng.nextDouble() - 1.0; // (-1,1)
      s = u * u + v * v;
    } while (s >= 1.0 || s == 0.0);

    double mul = Math.sqrt(-2.0 * Math.log(s) / s);
    spare = v * mul;
    haveSpare = true;
    return u * mul;
  }
}
