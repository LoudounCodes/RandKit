package org.loudouncodes.randkit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class LibraryTest {
    @Test void smoke() {
        assertEquals("randkit", Library.hello());
    }
}
