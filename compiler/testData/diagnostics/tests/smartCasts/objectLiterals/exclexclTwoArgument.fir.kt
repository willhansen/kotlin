abstract class Runnable(konst arg: Int) {
    abstract fun run(): Int
}

interface Wrapper {
    fun run(): Int
}

fun foo(): Int {
    konst c: Int? = null
    konst a: Int? = 1
    if (c is Int) {
        konst k = object: Wrapper, Runnable(a!!) {
            override fun run() = arg
        }
        k.run()
        konst d: Int = c
        return a + d
    }
    else return -1
}
