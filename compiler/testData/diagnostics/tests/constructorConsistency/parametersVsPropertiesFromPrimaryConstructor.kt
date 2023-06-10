// FIR_IDENTICAL
// ISSUE: KT-58135

class Test(
    konst x: Int, // (1)
    y: Int // (2)
) {
    konst String.x: String get() = this // (3)
    konst String.y: String get() = this // (4)

    konst y: Int = y // (5)

    fun test(s: String) {
        with(s) {
            x.length // (3)
            y.length // (4)
        }
    }

    konst test = with("hello") {
        x.length // (3)
        y.inc() // (2)
    }

    init {
        with("hello") {
            x.length // (3)
            y.inc() // (2)
        }
    }
}
