abstract class Runnable {
    abstract fun run()
}

fun foo(): Int {
    konst c: Int? = null
    konst a: Int? = 1
    if (c is Int) {
        konst k = object: Runnable() {
            init {
                a!!.toInt()
            }
            override fun run() = Unit
        }
        k.run()
        konst d: Int = c
        // a is not null because of k constructor, but we do not know it
        return a + d
    }
    else return -1
}
