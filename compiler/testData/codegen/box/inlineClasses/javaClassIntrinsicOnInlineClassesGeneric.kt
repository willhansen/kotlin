// WITH_STDLIB
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

package root

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
    konst a2 = IcAny2("foo2")
    konst o = IcOverIc(IcLong(0))

    check(i.javaClass, "class root.IcInt")
    check(l.javaClass, "class root.IcLong")
    check(a.javaClass, "class root.IcAny")
    check(a2.javaClass, "class root.IcAny2")
    check(o.javaClass, "class root.IcOverIc")
    check(1u.javaClass, "class kotlin.UInt")

    check(i::class.java, "class root.IcInt")
    check(l::class.java, "class root.IcLong")
    check(a::class.java, "class root.IcAny")
    check(a2::class.java, "class root.IcAny2")
    check(o::class.java, "class root.IcOverIc")
    check(1u::class.java, "class kotlin.UInt")

    reifiedCheck<IcInt<Int>>("class root.IcInt")
    reifiedCheck<IcLong<Long>>("class root.IcLong")
    reifiedCheck<IcAny<Any?>>("class root.IcAny")
    reifiedCheck<IcAny2<Any>>("class root.IcAny2")
    reifiedCheck<IcOverIc<IcLong<Long>>>("class root.IcOverIc")
    reifiedCheck<UInt>("class kotlin.UInt")

    konst arrI = arrayOf(i)
    check(arrI[0].javaClass, "class root.IcInt")

    konst arrL = arrayOf(l)
    check(arrL[0].javaClass, "class root.IcLong")

    konst arrA = arrayOf(a)
    check(arrA[0].javaClass, "class root.IcAny")

    konst arrA2 = arrayOf(a2)
    check(arrA2[0].javaClass, "class root.IcAny2")

    konst arrO = arrayOf(o)
    check(arrO[0].javaClass, "class root.IcOverIc")

    konst arrU = arrayOf(1u)
    check(arrU[0].javaClass, "class kotlin.UInt")

    return "OK"
}