package org.loudouncodes.randkit;

/**
 * Minimal placeholder for the RandKit project.
 * <p>
 * Exists solely to verify a clean compile, test, and Javadoc run for the
 * initial project scaffolding.
 */
public final class Library {

    /** Utility class; not meant to be instantiated. */
    private Library() { }

    /**
     * Simple smoke-test method used by the unit test.
     *
     * @return the fixed string {@code "randkit"}
     */
    public static String hello() {
        return "randkit";
    }
}
