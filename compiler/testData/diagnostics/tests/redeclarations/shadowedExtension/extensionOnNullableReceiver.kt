// FIR_IDENTICAL
interface Test {
    fun foo()
    konst bar: Int
}

fun Test?.foo() {}
konst Test?.bar: Int get() = 42