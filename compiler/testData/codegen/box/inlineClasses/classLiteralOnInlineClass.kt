// WITH_STDLIB
// WITH_REFLECT
// TARGET_BACKEND: JVM
// LANGUAGE: +ValueClasses

package root

import kotlin.reflect.KClass

@JvmInline
konstue class IcInt(konst x: Int)

@JvmInline
konstue class IcLong(konst l: Long)

@JvmInline
konstue class IcAny(konst a: Any?)

@JvmInline
konstue class IcOverIc(konst o: IcLong)

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
    konst o = IcOverIc(IcLong(0))

    check(i::class, "class root.IcInt")
    check(l::class, "class root.IcLong")
    check(a::class, "class root.IcAny")
    check(o::class, "class root.IcOverIc")
    check(1u::class, "class kotlin.UInt")

    check(i::class.simpleName, "IcInt")
    check(l::class.simpleName, "IcLong")
    check(a::class.simpleName, "IcAny")
    check(o::class.simpleName, "IcOverIc")
    check(1u::class.simpleName, "UInt")

    reifiedCheck<IcInt>("class root.IcInt", "IcInt")
    reifiedCheck<IcLong>("class root.IcLong", "IcLong")
    reifiedCheck<IcAny>("class root.IcAny", "IcAny")
    reifiedCheck<IcOverIc>("class root.IcOverIc", "IcOverIc")
    reifiedCheck<UInt>("class kotlin.UInt", "UInt")

    konst arrI = arrayOf(i)
    check(arrI[0]::class, "class root.IcInt")

    konst arrL = arrayOf(l)
    check(arrL[0]::class, "class root.IcLong")

    konst arrA = arrayOf(a)
    check(arrA[0]::class, "class root.IcAny")

    konst arrO = arrayOf(o)
    check(arrO[0]::class, "class root.IcOverIc")

    konst arrU = arrayOf(1u)
    check(arrU[0]::class, "class kotlin.UInt")

    check(IcInt::class, "class root.IcInt")
    check(IcLong::class, "class root.IcLong")
    check(IcAny::class, "class root.IcAny")
    check(IcOverIc::class, "class root.IcOverIc")
    check(UInt::class, "class kotlin.UInt")

    return "OK"
}