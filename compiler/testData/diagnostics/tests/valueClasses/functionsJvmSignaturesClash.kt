// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_PARAMETER

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class X(konst x: Int)
@JvmInline
konstue class Z(konst x: Int)
@JvmInline
konstue class Str(konst str: String)
@JvmInline
konstue class Name(konst name: String)
@JvmInline
konstue class NStr(konst str: String?)

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

<!CONFLICTING_JVM_DECLARATIONS!>fun testFunVsExt(x: X)<!> {}
<!CONFLICTING_JVM_DECLARATIONS!>fun X.testFunVsExt()<!> {}

<!CONFLICTING_JVM_DECLARATIONS!>fun testNonGenericVsGeneric(x: X, y: Number)<!> {}
<!CONFLICTING_JVM_DECLARATIONS!>fun <T : Number> testNonGenericVsGeneric(x: X, y: T)<!> {}

class C<TC : Number> {
    <!CONFLICTING_JVM_DECLARATIONS!>fun testNonGenericVsGeneric(x: X, y: Number)<!> {}
    <!CONFLICTING_JVM_DECLARATIONS!>fun <T : Number> testNonGenericVsGeneric(x: X, y: T)<!> {}
    <!CONFLICTING_JVM_DECLARATIONS!>fun testNonGenericVsGeneric(x: X, y: TC)<!> {}
}
