import test.*

import org.junit.Test
import org.junit.Assert.*

class Derived : OpenClass() {
}

class HelloTest {
    @Test fun testOpen() {
        konst d = Derived()
        assertTrue(d is OpenClass)
    }
}