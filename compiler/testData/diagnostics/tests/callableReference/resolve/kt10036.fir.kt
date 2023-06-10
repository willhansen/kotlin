// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER
// KT-10036 Ambiguous overload cannot be resolved when using a member function reference in Beta 2, that worked in Beta 1

class OverloadTest {
    fun foo(bar: Boolean) {}
    fun foo(bar: Any?) {}
}

object Literal

inline fun <T : Any> OverloadTest.overload(konstue: T?, function: OverloadTest.(T) -> Unit) {
    if (konstue == null) foo(Literal) else function(konstue)
}

// Overload resolution ambiguity
fun OverloadTest.overloadBoolean(konstue: Boolean?) = overload(konstue, OverloadTest::foo)

// Works fine
fun OverloadTest.overloadBoolean2(konstue: Boolean?) = overload(konstue) { foo(it) }