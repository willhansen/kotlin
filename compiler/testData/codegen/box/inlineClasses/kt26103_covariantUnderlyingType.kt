// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: ANDROID
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GList<T>(konst xs: List<T>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GList2<T: Any>(konst xs: List<T?>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GSList<T>(konst ss: List<String>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class SList(konst ss: List<String>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IList(konst ints: List<Int>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GIList<T>(konst ints: List<Int>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class II(konst i: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IIList(konst iis: List<II>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GIIList<T>(konst iis: List<II>)

fun testGList(gl: GList<String>) {
    if (gl.xs[0] != "OK") throw AssertionError()
}

fun testGList2(gl: GList2<String>) {
    if (gl.xs[0] != "OK") throw AssertionError()
}

fun testGSList(sl: GSList<String>) {
    if (sl.ss[0] != "OK") throw AssertionError()
}

fun testSList(sl: SList) {
    if (sl.ss[0] != "OK") throw AssertionError()
}

fun testIList(il: IList) {
    if (il.ints[0] != 42) throw AssertionError()
}

fun testGIList(gil: GIList<Any>) {
    if (gil.ints[0] != 42) throw AssertionError()
}

fun testIIList(iil: IIList) {
    if (iil.iis[0].i != 42) throw AssertionError()
}

fun testGIIList(giil: GIIList<Any>) {
    if (giil.iis[0].i != 42) throw AssertionError()
}

fun box(): String {
    testGList(GList(listOf("OK")))
    testGList2(GList2(listOf("OK")))
    testGSList(GSList(listOf("OK")))
    testSList(SList(listOf("OK")))
    testIList(IList(listOf(42)))
    testGIList(GIList<Any>(listOf(42)))
    testIIList(IIList(listOf(II(42))))
    testGIIList(GIIList<Any>(listOf(II(42))))

    return "OK"
}