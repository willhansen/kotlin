// FIR_IDENTICAL
interface Wise {
    fun doIt(): Int
}

fun Wise(f: () -> Int) = object: Wise {
    override fun doIt() = f()
}

class My {
    // Still dangerous (???), nobogy can guarantee what Wise() will do with this lambda
    konst x = Wise { foo() }

    konst y = 42

    fun foo() = y

    fun bar() = x.doIt()
}