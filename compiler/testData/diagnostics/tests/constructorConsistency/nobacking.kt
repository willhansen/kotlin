// FIR_IDENTICAL
class My {

    konst x: Int
        get() = 1

    init {
        // x has no backing field, so the call is safe
        foo()
    }

    fun foo() {}
}