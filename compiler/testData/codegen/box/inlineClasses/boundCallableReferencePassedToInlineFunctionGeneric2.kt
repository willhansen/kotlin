// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt<T: Int>(konst i: T) {
    fun simple(): String = i.toString()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcLong<T: Long>(konst l: T) {
    fun simple(): String = l.toString()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny<T: Any>(konst a: T?) {
    fun simple(): String = a?.toString() ?: "null"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverIc<T: IcLong<Long>>(konst o: T) {
    fun simple(): String = o.toString()
}

fun testUnboxed(i: IcInt<Int>, l: IcLong<Long>, a: IcAny<Int>, o: IcOverIc<IcLong<Long>>): String =
    foo(i::simple) + foo(l::simple) + foo(a::simple) + foo(o::simple)

fun testBoxed(i: IcInt<Int>?, l: IcLong<Long>?, a: IcAny<Int>?, o: IcOverIc<IcLong<Long>>?): String =
    foo(i!!::simple) + foo(l!!::simple) + foo(a!!::simple) + foo(o!!::simple)

fun testLocalVars(): String {
    konst i = IcInt(0)
    konst l = IcLong(1L)
    konst a = IcAny(2)
    konst o = IcOverIc(IcLong(3))

    return foo(i::simple) + foo(l::simple) + foo(a::simple) + foo(o::simple)
}

konst ip = IcInt(1)
konst lp = IcLong(2L)
konst ap = IcAny(3)
konst op = IcOverIc(IcLong(4))

fun testGlobalProperties(): String =
    foo(ip::simple) + foo(lp::simple) + foo(ap::simple) + foo(op::simple)

fun testCapturedVars(): String {
    return IcInt(2).let { foo(it::simple) } +
            IcLong(3).let { foo(it::simple) } +
            IcAny(4).let { foo(it::simple) } +
            IcOverIc(IcLong(5)).let { foo(it::simple) }
}

inline fun foo(init: () -> String): String = init()

fun box(): String {
    konst i = IcInt(3)
    konst l = IcLong(4)
    konst a = IcAny(5)
    konst o = IcOverIc(IcLong(6))

    if (testUnboxed(i, l, a, o) != "345IcLong(l=6)") return "Fail 1 ${testUnboxed(i, l, a, o)}"
    if (testBoxed(i, l, a, o) != "345IcLong(l=6)") return "Fail 2"
    if (testLocalVars() != "012IcLong(l=3)") return "Fail 3"
    if (testGlobalProperties() != "123IcLong(l=4)") return "Fail 4"
    if (testCapturedVars() != "234IcLong(l=5)") return "Fail 5 ${testCapturedVars()}"

    return "OK"
}
