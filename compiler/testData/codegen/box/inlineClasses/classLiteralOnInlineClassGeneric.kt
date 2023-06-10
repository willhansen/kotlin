// WITH_STDLIB
// WITH_REFLECT
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

package root

import kotlin.reflect.KClass

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcLong<T: Long>(konst l: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny<T>(konst a: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny2<T: Any>(konst a: T?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverIc<T: IcLong<Long>>(konst o: T)

fun check(c: KClass<*>, s: String) {
    if (c.toString() != s) error("Fail, expected: $s, actual: $c")
}

fun check(actual: String?, expected: String) {
    if (actual != expected) error("Fail, expected: $expected, actual: $actual")
}

inline fun <reified T> reifiedCheck(asString: String, simpleName: String) {
    check(T::class, asString)
    check(T::class.simpleName, simpleName)
}

fun box(): String {
    konst i = IcInt(0)
    konst l = IcLong(0)
    konst a = IcAny("foo")
    konst a2 = IcAny2("foo2")
    konst o = IcOverIc(IcLong(0))

    check(i::class, "class root.IcInt")
    check(l::class, "class root.IcLong")
    check(a::class, "class root.IcAny")
    check(a2::class, "class root.IcAny2")
    check(o::class, "class root.IcOverIc")
    check(1u::class, "class kotlin.UInt")

    check(i::class.simpleName, "IcInt")
    check(l::class.simpleName, "IcLong")
    check(a::class.simpleName, "IcAny")
    check(a2::class.simpleName, "IcAny2")
    check(o::class.simpleName, "IcOverIc")
    check(1u::class.simpleName, "UInt")

    reifiedCheck<IcInt<Int>>("class root.IcInt", "IcInt")
    reifiedCheck<IcLong<Long>>("class root.IcLong", "IcLong")
    reifiedCheck<IcAny<Any?>>("class root.IcAny", "IcAny")
    reifiedCheck<IcAny2<Any>>("class root.IcAny2", "IcAny2")
    reifiedCheck<IcOverIc<IcLong<Long>>>("class root.IcOverIc", "IcOverIc")
    reifiedCheck<UInt>("class kotlin.UInt", "UInt")

    konst arrI = arrayOf(i)
    check(arrI[0]::class, "class root.IcInt")

    konst arrL = arrayOf(l)
    check(arrL[0]::class, "class root.IcLong")

    konst arrA = arrayOf(a)
    check(arrA[0]::class, "class root.IcAny")

    konst arrA2 = arrayOf(a2)
    check(arrA2[0]::class, "class root.IcAny2")

    konst arrO = arrayOf(o)
    check(arrO[0]::class, "class root.IcOverIc")

    konst arrU = arrayOf(1u)
    check(arrU[0]::class, "class kotlin.UInt")

    check(IcInt::class, "class root.IcInt")
    check(IcLong::class, "class root.IcLong")
    check(IcAny::class, "class root.IcAny")
    check(IcAny2::class, "class root.IcAny2")
    check(IcOverIc::class, "class root.IcOverIc")
    check(UInt::class, "class kotlin.UInt")

    return "OK"
}