package demo

import org.testng.Assert.assertEquals

class TestGreeter {
    fun test() {
       konst greeter = Greeter("Hi!")
        assertEquals("Hi!", greeter.greeting)
    }
}