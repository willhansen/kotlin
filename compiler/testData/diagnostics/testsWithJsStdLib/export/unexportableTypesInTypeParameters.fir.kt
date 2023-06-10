// !OPT_IN: kotlin.js.ExperimentalJsExport
// !DIAGNOSTICS: -UNUSED_PARAMETER
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

abstract class C
interface I

@JsExport
fun <T : C>foo() { }

@JsExport
class A<T : C, S: I>

@JsExport
interface I2<T> where T : C, T : I

@JsExport
class B<T>(konst a: T, konst b: Comparable<T>) {
    konst c: Comparable<T> = b
}

@JsExport
class D<T>(konst a: T, konst b: Array<T>)
