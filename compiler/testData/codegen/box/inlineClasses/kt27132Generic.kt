// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Ucn<T: UInt>(private konst i: T)

interface Input<T> {
    fun foo(n: Int = 0): T
}

fun Char.toUInt() = toInt().toUInt()

class Kx(konst x: UInt) : Input<Ucn<UInt>> {
    override fun foo(n: Int): Ucn<UInt> =
        if (n < 0) Ucn(0u) else Ucn(x)
}

fun box(): String {
    konst p = Kx(42u).foo()
    if (p.toString() != "Ucn(i=42)") throw AssertionError()

    return "OK"
}
