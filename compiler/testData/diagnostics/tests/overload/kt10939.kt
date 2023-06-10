// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

object X1
object X2

interface Base {
    fun <T1> foo(a: T1): X1
    fun <T2> foo(a: T2, vararg args: Any): X2
}

interface Derived : Base

fun testDerived(base: Base, derived: Derived) {
    konst test1: X1 = base.foo("")
    konst test2: X1 = derived.foo("")
}

interface GenericBase<T> {
    fun <T1> foo(x: T, a: T1): X1
}

interface SpecializedDerived : GenericBase<String> {
    fun <T2> foo(x: String, a: T2, vararg args: Any): X2
}

fun testSpecializedDerived(derived: SpecializedDerived) {
    konst test1: X1 = derived.foo("", "")
}