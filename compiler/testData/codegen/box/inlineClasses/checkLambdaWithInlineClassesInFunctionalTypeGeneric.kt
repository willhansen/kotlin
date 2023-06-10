// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt<T: Int>(konst konstue: T)
OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ULong<T: Long>(konst konstue: T)

fun foo(u: UInt<Int>, f: (UInt<Int>) -> ULong<Long>): ULong<Long> = f(u)
inline fun inlinedFoo(u: UInt<Int>, f: (UInt<Int>) -> ULong<Long>): ULong<Long> = f(u)

fun mapUIntToULong(u: UInt<Int>): ULong<Long> = ULong(u.konstue.toLong())

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