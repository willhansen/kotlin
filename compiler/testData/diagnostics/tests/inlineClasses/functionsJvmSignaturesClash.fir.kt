// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER

inline class X(konst x: Int)
inline class Z(konst x: Int)
inline class Str(konst str: String)
inline class Name(konst name: String)
inline class NStr(konst str: String?)

fun testSimple(x: X) {}
fun testSimple(z: Z) {}

fun testMixed(x: Int, y: Int) {}
fun testMixed(x: X, y: Int) {}
fun testMixed(x: Int, y: X) {}
fun testMixed(x: X, y: X) {}

fun testNewType(s: Str) {}
fun testNewType(name: Name) {}

fun testNullableVsNonNull1(s: Str) {}
fun testNullableVsNonNull1(s: Str?) {}

fun testNullableVsNonNull2(ns: NStr) {}
fun testNullableVsNonNull2(ns: NStr?) {}

fun testFunVsExt(x: X) {}
fun X.testFunVsExt() {}

fun testNonGenericVsGeneric(x: X, y: Number) {}
fun <T : Number> testNonGenericVsGeneric(x: X, y: T) {}

class C<TC : Number> {
    fun testNonGenericVsGeneric(x: X, y: Number) {}
    fun <T : Number> testNonGenericVsGeneric(x: X, y: T) {}
    fun testNonGenericVsGeneric(x: X, y: TC) {}
}