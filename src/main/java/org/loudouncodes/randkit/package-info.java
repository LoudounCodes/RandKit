/**
 * RandKit — lightweight random variate generation for Java 17+.
 *
 * <h2>What this package is</h2>
 *
 * <p>A small, opinionated library for sampling from probability distributions, built directly on
 * the JDK {@code java.util.random.RandomGenerator} API. It aims to be deterministic by seed,
 * bias-free for integer ranges, and easy to use in simulations and teaching.
 *
 * <h2>Key concepts</h2>
 *
 * <ul>
 *   <li><b>Distributions:</b> Continuous and discrete types expose a small, consistent surface
 *       (sample, pdf/pmf, cdf, quantile, mean, variance) via {@link
 *       org.loudouncodes.randkit.api.ContinuousDistribution} and {@link
 *       org.loudouncodes.randkit.api.DiscreteDistribution}.
 *   <li><b>Support metadata:</b> Distributions describe their mathematical domain using {@link
 *       org.loudouncodes.randkit.api.DistributionSupport} so callers can reason about bounds,
 *       open/closed endpoints, and discrete vs. continuous values.
 *   <li><b>Samplers:</b> For “just draw values” scenarios, the tiny functional interfaces {@link
 *       org.loudouncodes.randkit.api.DoubleSampler} and {@link
 *       org.loudouncodes.randkit.api.IntSampler} provide minimal plugs.
 *   <li><b>RNG utilities:</b> {@link org.loudouncodes.randkit.util.Randoms} centralizes the default
 *       generator choice and seeded construction.
 * </ul>
 *
 * <h2>Seeding &amp; determinism</h2>
 *
 * <p>All distributions accept a {@code RandomGenerator}. Overloads are provided for a sensible
 * default RNG and for deterministic, seeded construction. Given the same algorithm, seed, and
 * parameters, sequences are repeatable.
 *
 * <h2>Threading</h2>
 *
 * <p>Distribution instances are not synchronized. Prefer one instance per thread, or supply a
 * {@code RandomGenerator} that you split per thread. Avoid sharing a single instance across threads
 * unless you provide external synchronization.
 *
 * <h2>Integer sampling (no modulo bias)</h2>
 *
 * <p>Integer-valued generators use unbiased methods (e.g., bounded {@code nextLong} or rejection
 * sampling) for ranges that are not powers of two.
 *
 * <h2>What’s included (v1 snapshot)</h2>
 *
 * <ul>
 *   <li>Continuous: {@link org.loudouncodes.randkit.continuous.UniformDouble} (uniform on [a,b)).
 *   <li>Discrete: {@link org.loudouncodes.randkit.discrete.UniformInt} (uniform on integers [a,b]).
 * </ul>
 *
 * <h2>Quick start</h2>
 *
 * <pre>{@code
 * import java.util.random.RandomGenerator;
 * import org.loudouncodes.randkit.continuous.UniformDouble;
 * import org.loudouncodes.randkit.discrete.UniformInt;
 * import org.loudouncodes.randkit.util.Randoms;
 *
 * // Continuous uniform on [0,1), default RNG
 * double x = new UniformDouble(0.0, 1.0).sample();
 *
 * // Discrete uniform on {1,2,3,4,5,6}, deterministic by seed
 * int face = new UniformInt(1234L, 1, 6).sample();
 *
 * // Bring your own generator
 * RandomGenerator rng = Randoms.seeded(42L);
 * double y = new UniformDouble(rng, -2.0, 3.0).sample();
 * }</pre>
 *
 * <h2>Validation &amp; errors</h2>
 *
 * <p>Constructors validate parameters eagerly and throw {@link java.lang.IllegalArgumentException}
 * for invalid inputs (e.g., {@code a < b} for uniform, probabilities in [0,1], positive scale).
 *
 * <h2>Subpackages</h2>
 *
 * <ul>
 *   <li>{@code org.loudouncodes.randkit.api} — public interfaces and support metadata.
 *   <li>{@code org.loudouncodes.randkit.continuous} — continuous distributions.
 *   <li>{@code org.loudouncodes.randkit.discrete} — discrete distributions.
 *   <li>{@code org.loudouncodes.randkit.util} — RNG utilities and general helpers.
 * </ul>
 *
 * @since 0.1.0
 */
package org.loudouncodes.randkit;
