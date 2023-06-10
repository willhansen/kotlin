// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: ANDROID
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GCmp<T>(konst xc: Comparable<T>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GSCmp<T>(konst sc: Comparable<String>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class SCmp(konst sc: Comparable<String>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ICmp(konst intc: Comparable<Int>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GICmp<T>(konst intc: Comparable<Int>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class II(konst i: Int) : Comparable<II> {
    override fun compareTo(other: II): Int {
        return i.compareTo(other.i)
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IICmp(konst iic: Comparable<II>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GIICmp<T>(konst iic: Comparable<II>)

fun testGCmp(x: GCmp<String>) {
    if (x.xc.compareTo("OK") != 0) throw AssertionError()
}

fun testGSCmp(x: GSCmp<Any>) {
    if (x.sc.compareTo("OK") != 0) throw AssertionError()
}

fun testSCmp(x: SCmp) {
    if (x.sc.compareTo("OK") != 0) throw AssertionError()
}

fun testICmp(x: ICmp) {
    if (x.intc.compareTo(42) != 0) throw AssertionError()
}

fun testGICmp(x: GICmp<Any>) {
    if (x.intc.compareTo(42) != 0) throw AssertionError()
}

fun testIICmp(x: IICmp) {
    if (x.iic.compareTo(II(42)) != 0) throw AssertionError()
}

fun testGIICmp(x: GIICmp<Any>) {
    if (x.iic.compareTo(II(42)) != 0) throw AssertionError()
}

fun box(): String {
    testGCmp(GCmp("OK"))
    testGSCmp(GSCmp<Any>("OK"))
    testSCmp(SCmp("OK"))
    testICmp(ICmp(42))
    testGICmp(GICmp<Any>(42))
    testIICmp(IICmp(II(42)))
    testGIICmp(GIICmp<Any>(II(42)))

    return "OK"
}