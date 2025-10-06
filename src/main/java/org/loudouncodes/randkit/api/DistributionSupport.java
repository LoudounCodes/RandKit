package org.loudouncodes.randkit.api;

/**
 * Describes the mathematical support (domain) of a probability distribution:
 * numeric lower/upper bounds, whether each bound is closed (inclusive) or open
 * (exclusive), and whether values are treated as continuous reals or discrete integers.
 * <p>
 * Typical examples:
 * <ul>
 *   <li>Normal: {@code (-∞, +∞)} continuous</li>
 *   <li>Exponential: {@code [0, +∞)} continuous</li>
 *   <li>Poisson: {@code {0,1,2,...}} discrete</li>
 *   <li>Beta: {@code (0,1)} continuous (open interval)</li>
 * </ul>
 */
public final class DistributionSupport {

    /**
     * Indicates whether the distribution's values are continuous reals or discrete integers.
     */
    public enum Kind {
        /** Continuous, real-valued support. */
        CONTINUOUS,
        /** Discrete, integer-valued support. */
        DISCRETE
    }

    private final double lower;
    private final double upper;
    private final boolean lowerClosed;
    private final boolean upperClosed;
    private final Kind kind;

    /**
     * Creates a new support descriptor.
     *
     * @param lower the numeric lower bound, or {@link Double#NEGATIVE_INFINITY} for unbounded below
     * @param lowerClosed {@code true} if the lower bound is included (closed); ignored if unbounded below
     * @param upper the numeric upper bound, or {@link Double#POSITIVE_INFINITY} for unbounded above
     * @param upperClosed {@code true} if the upper bound is included (closed); ignored if unbounded above
     * @param kind whether values are continuous reals or discrete integers
     * @throws IllegalArgumentException if finite bounds are not strictly ordered ({@code lower < upper})
     */
    private DistributionSupport(
            double lower, boolean lowerClosed,
            double upper, boolean upperClosed,
            Kind kind) {
        if (!Double.isFinite(lower) && lower != Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("lower must be finite or -Infinity");
        }
        if (!Double.isFinite(upper) && upper != Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("upper must be finite or +Infinity");
        }
        if (Double.isFinite(lower) && Double.isFinite(upper) && !(lower < upper)) {
            throw new IllegalArgumentException("lower must be strictly less than upper");
        }
        this.lower = lower;
        this.upper = upper;
        this.lowerClosed = lowerClosed;
        this.upperClosed = upperClosed;
        this.kind = kind;
    }

    /**
     * Returns the numeric lower bound.
     *
     * @return the lower bound, or {@link Double#NEGATIVE_INFINITY} if unbounded below
     */
    public double lower() { return lower; }

    /**
     * Returns the numeric upper bound.
     *
     * @return the upper bound, or {@link Double#POSITIVE_INFINITY} if unbounded above
     */
    public double upper() { return upper; }

    /**
     * Indicates whether the lower bound is included in the support (closed).
     * Ignored when the support is unbounded below.
     *
     * @return {@code true} if the lower bound is closed; {@code false} otherwise
     */
    public boolean isLowerClosed() { return lowerClosed; }

    /**
     * Indicates whether the upper bound is included in the support (closed).
     * Ignored when the support is unbounded above.
     *
     * @return {@code true} if the upper bound is closed; {@code false} otherwise
     */
    public boolean isUpperClosed() { return upperClosed; }

    /**
     * Returns whether the support is unbounded below.
     *
     * @return {@code true} if the lower bound is {@link Double#NEGATIVE_INFINITY}; otherwise {@code false}
     */
    public boolean isUnboundedBelow() { return lower == Double.NEGATIVE_INFINITY; }

    /**
     * Returns whether the support is unbounded above.
     *
     * @return {@code true} if the upper bound is {@link Double#POSITIVE_INFINITY}; otherwise {@code false}
     */
    public boolean isUnboundedAbove() { return upper == Double.POSITIVE_INFINITY; }

    /**
     * Returns the value kind (continuous or discrete).
     *
     * @return the support kind
     */
    public Kind kind() { return kind; }

    /**
     * Tests whether a numeric value lies within this support's interval,
     * respecting open/closed endpoints. For discrete supports, this method
     * checks only the numeric interval; it does not require {@code x} to be an integer.
     *
     * @param x the value to test
     * @return {@code true} if {@code x} lies in the support; {@code false} otherwise
     */
    public boolean contains(double x) {
        boolean aboveLower = isUnboundedBelow()
                || (lowerClosed ? x >= lower : x > lower);
        boolean belowUpper = isUnboundedAbove()
                || (upperClosed ? x <= upper : x < upper);
        return aboveLower && belowUpper;
    }

    /**
     * Creates a continuous (real-valued) support with the given bounds.
     *
     * @param lower the lower bound, or {@link Double#NEGATIVE_INFINITY} for unbounded below
     * @param lowerClosed {@code true} if the lower bound is closed (included)
     * @param upper the upper bound, or {@link Double#POSITIVE_INFINITY} for unbounded above
     * @param upperClosed {@code true} if the upper bound is closed (included)
     * @return a continuous {@code DistributionSupport}
     */
    public static DistributionSupport continuous(
            double lower, boolean lowerClosed,
            double upper, boolean upperClosed) {
        return new DistributionSupport(lower, lowerClosed, upper, upperClosed, Kind.CONTINUOUS);
    }

    /**
     * Creates a discrete (integer-valued) support with the given bounds.
     *
     * @param lower the lower bound, or {@link Double#NEGATIVE_INFINITY} for unbounded below
     * @param lowerClosed {@code true} if the lower bound is closed (included)
     * @param upper the upper bound, or {@link Double#POSITIVE_INFINITY} for unbounded above
     * @param upperClosed {@code true} if the upper bound is closed (included)
     * @return a discrete {@code DistributionSupport}
     */
    public static DistributionSupport discrete(
            double lower, boolean lowerClosed,
            double upper, boolean upperClosed) {
        return new DistributionSupport(lower, lowerClosed, upper, upperClosed, Kind.DISCRETE);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        String l = isUnboundedBelow() ? "(-Inf" : (lowerClosed ? "[" + lower : "(" + lower);
        String r = isUnboundedAbove() ? "+Inf)" : (upperClosed ? upper + "]" : upper + ")");
        return "Support" + l + ", " + r + " " + kind + ")";
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int h = 17;
        long a = Double.doubleToLongBits(lower);
        long b = Double.doubleToLongBits(upper);
        h = 31 * h + (int)(a ^ (a >>> 32));
        h = 31 * h + (int)(b ^ (b >>> 32));
        h = 31 * h + (lowerClosed ? 1 : 0);
        h = 31 * h + (upperClosed ? 1 : 0);
        h = 31 * h + kind.hashCode();
        return h;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DistributionSupport)) return false;
        DistributionSupport d = (DistributionSupport) o;
        return Double.doubleToLongBits(lower) == Double.doubleToLongBits(d.lower)
                && Double.doubleToLongBits(upper) == Double.doubleToLongBits(d.upper)
                && lowerClosed == d.lowerClosed
                && upperClosed == d.upperClosed
                && kind == d.kind;
    }

    /* ---------- Common presets ---------- */

    /** Real line {@code (-∞, +∞)}, continuous. */
    public static final DistributionSupport REAL_LINE =
            continuous(Double.NEGATIVE_INFINITY, false, Double.POSITIVE_INFINITY, false);

    /** Non-negative reals {@code [0, +∞)}, continuous. */
    public static final DistributionSupport NON_NEGATIVE_REALS =
            continuous(0.0, true, Double.POSITIVE_INFINITY, false);

    /** Unit interval {@code (0, 1)}, continuous (open). */
    public static final DistributionSupport UNIT_INTERVAL_OPEN =
            continuous(0.0, false, 1.0, false);

    /** Unit interval {@code [0, 1]}, continuous (closed). */
    public static final DistributionSupport UNIT_INTERVAL_CLOSED =
            continuous(0.0, true, 1.0, true);

    /** Non-negative integers {@code {0,1,2,...}}, discrete. */
    public static final DistributionSupport NON_NEGATIVE_INTEGERS =
            discrete(0.0, true, Double.POSITIVE_INFINITY, false);
}
