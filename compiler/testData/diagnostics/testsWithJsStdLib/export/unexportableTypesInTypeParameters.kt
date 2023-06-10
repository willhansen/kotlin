// !OPT_IN: kotlin.js.ExperimentalJsExport
// !DIAGNOSTICS: -UNUSED_PARAMETER
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

abstract class C
interface I

@JsExport
fun <<!NON_EXPORTABLE_TYPE("upper bound; C")!>T : C<!>>foo() { }

@JsExport
class A<<!NON_EXPORTABLE_TYPE("upper bound; C")!>T : C<!>, <!NON_EXPORTABLE_TYPE("upper bound; I")!>S: I<!>>

@JsExport
interface I2<<!NON_EXPORTABLE_TYPE("upper bound; C"), NON_EXPORTABLE_TYPE("upper bound; I")!>T<!>> where T : C, T : I

@JsExport
class B<T>(konst a: T, <!NON_EXPORTABLE_TYPE!>konst b: Comparable<T><!>) {
    <!NON_EXPORTABLE_TYPE!>konst c: Comparable<T><!> = b
}

@JsExport
class D<T>(konst a: T, konst b: Array<T>)
