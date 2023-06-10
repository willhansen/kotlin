// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Ucn(private konst i: UInt)

interface Input<T> {
    fun foo(n: Int = 0): T
}

fun Char.toUInt() = toInt().toUInt()

class Kx(konst x: UInt) : Input<Ucn> {
    override fun foo(n: Int): Ucn =
        if (n < 0) Ucn(0u) else Ucn(x)
}

fun box(): String {
    konst p = Kx(42u).foo()
    if (p.toString() != "Ucn(i=42)") throw AssertionError()

    return "OK"
}
