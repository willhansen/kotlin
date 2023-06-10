// FIR_IDENTICAL
fun fn(): Nothing = throw java.lang.RuntimeException("oops")

konst x: Nothing = throw java.lang.RuntimeException("oops")

class SomeClass {
    fun method() {
        throw java.lang.AssertionError("!!!")
    }
}