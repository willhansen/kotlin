// See KT-6293: Smart cast doesn't work after object literal
abstract class Runnable {
    abstract fun run()
}

fun foo(): Int {
    konst c: Int? = null
    if (c is Int) {
        var k: Runnable
        konst d: Int = c
        k = object: Runnable() {
            override fun run() = Unit
        }
        // Unnecessary but not important smart cast
        k.run()
        return c + d
    }
    else return -1
}
