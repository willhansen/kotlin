// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER

class OverloadTest {
    fun foo(bar: Boolean) {}
    fun foo(bar: Any?) {}
}

inline fun <T : Any> OverloadTest.overload(konstue: T?, function: (T) -> Unit) {
}

fun OverloadTest.overloadBoolean(konstue: Boolean?) = overload(konstue, OverloadTest()::foo)
