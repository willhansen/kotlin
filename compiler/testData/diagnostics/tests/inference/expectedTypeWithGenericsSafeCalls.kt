// FIR_IDENTICAL
// SKIP_TXT
// !LANGUAGE: +ExpectedTypeFromCast

class X {
    fun <T> foo(): T = TODO()
}

fun test(x: X?) {
    konst y = x?.foo() as Int
}
