// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(konst konstue: Int)
OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ULong(konst konstue: Long)

fun foo(u: UInt, f: (UInt) -> ULong): ULong = f(u)
inline fun inlinedFoo(u: UInt, f: (UInt) -> ULong): ULong = f(u)

fun mapUIntToULong(u: UInt): ULong = ULong(u.konstue.toLong())

fun box(): String {
    konst u = UInt(123)
    konst l1 = foo(u) {
        mapUIntToULong(it)
    }

    if (l1.konstue != 123L) return "fail"

    konst l2 = inlinedFoo(UInt(10)) {
        mapUIntToULong(it)
    }

    if (l2.konstue != 10L) return "fail"

    return "OK"
}