// See KT-6293: Smart cast doesn't work after object literal
abstract class Runnable {
    abstract fun run()
}

fun foo(): Int {
    konst c: Int? = null
    if (c is Int) {
        konst d: Int = c
        // This object breaks data flow info propagation
        konst k = object: Runnable() {
            override fun run() = Unit
        }
        k.run()
        // Smart cast should work but error is reported
        return c + d
    }
    else return -1
}
