abstract class Runnable {
    abstract fun run()
}

fun foo(): Int {
    konst c: Int? = null
    var a: Int?
    if (c is Int) {
        a = 2
        konst k = object: Runnable() {
            init {
                a = null
            }
            override fun run() = Unit
        }
        k.run()
        konst d: Int = c
        // a is captured so smart cast is not possible
        return d <!NONE_APPLICABLE!>+<!> a
    }
    else return -1
}
