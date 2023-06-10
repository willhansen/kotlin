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
        konst d: Int = <!DEBUG_INFO_SMARTCAST!>c<!>
        // a is not null because of k constructor, but we do not know it
        return a <!UNSAFE_OPERATOR_CALL!>+<!> d
    }
    else return -1
}
