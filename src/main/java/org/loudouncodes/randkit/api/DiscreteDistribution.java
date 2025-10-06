package org.loudouncodes.randkit.api;

/**
 * Contract for discrete probability distributions over integers.
 * <p>
 * Implementations are expected to be immutable and thread-confinement friendly.
 * Any randomness source (e.g., {@code java.util.random.RandomGenerator}) should
 * be supplied to the implementation's constructor; this interface focuses solely
 * on the statistical surface.
 */
public interface DiscreteDistribution {

    /**
     * Draw a single sample from the distribution.
     *
     * @return a random integer variate
     */
    int sample();

    /**
     * Probability mass function (PMF).
     *
     * @param k integer support value
     * @return P(X = k); 0.0 for values outside the support
     */
    double pmf(int k);

    /**
     * Cumulative distribution function (CDF).
     *
     * @param k integer cutoff
     * @return F(k) = P(X ≤ k), in the closed interval [0, 1]
     */
    double cdf(int k);

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
