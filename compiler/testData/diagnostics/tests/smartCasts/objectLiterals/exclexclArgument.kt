abstract class Runnable(konst arg: Int) {
    abstract fun run(): Int
}

fun foo(): Int {
    konst c: Int? = null
    konst a: Int? = 1
    if (c is Int) {
        konst k = object: Runnable(a!!) {
            override fun run() = arg
        }
        k.run()
        konst d: Int = <!DEBUG_INFO_SMARTCAST!>c<!>
        return <!DEBUG_INFO_SMARTCAST!>a<!> + d
    }
    else return -1
}
