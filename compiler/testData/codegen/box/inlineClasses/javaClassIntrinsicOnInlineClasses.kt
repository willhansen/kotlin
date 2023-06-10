// WITH_STDLIB
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

package root

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcLong(konst l: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny(konst a: Any?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverIc(konst o: IcLong)

fun check(c: Class<*>, s: String) {
    if (c.toString() != s) error("Fail, expected: $s, actual: $c")
}

inline fun <reified T> reifiedCheck(asString: String) {
    check(T::class.java, asString)
}

fun box(): String {
    konst i = IcInt(0)
    konst l = IcLong(0)
    konst a = IcAny("foo")
    konst o = IcOverIc(IcLong(0))

    check(i.javaClass, "class root.IcInt")
    check(l.javaClass, "class root.IcLong")
    check(a.javaClass, "class root.IcAny")
    check(o.javaClass, "class root.IcOverIc")
    check(1u.javaClass, "class kotlin.UInt")

    check(i::class.java, "class root.IcInt")
    check(l::class.java, "class root.IcLong")
    check(a::class.java, "class root.IcAny")
    check(o::class.java, "class root.IcOverIc")
    check(1u::class.java, "class kotlin.UInt")

    reifiedCheck<IcInt>("class root.IcInt")
    reifiedCheck<IcLong>("class root.IcLong")
    reifiedCheck<IcAny>("class root.IcAny")
    reifiedCheck<IcOverIc>("class root.IcOverIc")
    reifiedCheck<UInt>("class kotlin.UInt")

    konst arrI = arrayOf(i)
    check(arrI[0].javaClass, "class root.IcInt")

    konst arrL = arrayOf(l)
    check(arrL[0].javaClass, "class root.IcLong")

    konst arrA = arrayOf(a)
    check(arrA[0].javaClass, "class root.IcAny")

    konst arrO = arrayOf(o)
    check(arrO[0].javaClass, "class root.IcOverIc")

    konst arrU = arrayOf(1u)
    check(arrU[0].javaClass, "class kotlin.UInt")

    return "OK"
}