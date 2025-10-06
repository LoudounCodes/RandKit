package org.loudouncodes.randkit.api;

/**
 * Contract for continuous probability distributions.
 * <p>
 * Implementations are expected to be immutable and thread-confinement friendly.
 * Any randomness source (e.g., {@code java.util.random.RandomGenerator}) should
 * be supplied to the implementation's constructor; this interface focuses solely
 * on the statistical surface.
 */
public interface ContinuousDistribution {

    /**
     * Draw a single sample from the distribution.
     *
     * @return a random variate
     */
    double sample();

    /**
     * Probability density function (PDF).
     *
     * @param x point at which to evaluate the density
     * @return the density value f(x); 0.0 for points outside the support
     */
    double pdf(double x);

    /**
     * Cumulative distribution function (CDF).
     *
     * @param x point at which to evaluate the cumulative probability
     * @return F(x) = P(X ≤ x), in the closed interval [0, 1]
     */
    double cdf(double x);

    /**
     * Quantile function (inverse CDF).
     * <p>
     * For {@code p} in the open interval (0, 1), returns a value {@code q}
     * such that {@code cdf(q) ≈ p}. For {@code p == 0} or {@code p == 1},
     * implementations may return {@link Double#NEGATIVE_INFINITY} or
     * {@link Double#POSITIVE_INFINITY}, respectively, where appropriate.
     *
     * @param p cumulative probability, typically in [0, 1]
     * @return a quantile corresponding to {@code p}
     * @throws IllegalArgumentException if {@code p} is outside [0, 1]
     */
    double quantile(double p);

    /**
     * Theoretical mean of the distribution, when defined.
     *
     * @return the mean; may be {@link Double#NaN} if undefined
     */
    double mean();

    /**
     * Theoretical variance of the distribution, when defined.
     *
     * @return the variance; may be {@link Double#NaN} if undefined or infinite
     */
    double variance();
}
