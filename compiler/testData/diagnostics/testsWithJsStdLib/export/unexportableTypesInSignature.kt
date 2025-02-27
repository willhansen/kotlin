// !OPT_IN: kotlin.js.ExperimentalJsExport
// !DIAGNOSTICS: -UNUSED_PARAMETER
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

class C

@JsExport
fun foo(<!NON_EXPORTABLE_TYPE("parameter; C")!>x: C<!>) {
}

<!NON_EXPORTABLE_TYPE("return; C")!>@JsExport
fun bar()<!> = C()

<!NON_EXPORTABLE_TYPE("property; C")!>@JsExport
konst x: C<!> = C()

<!NON_EXPORTABLE_TYPE("property; C")!>@JsExport
var x2: C<!>
    get() = C()
    set(konstue) { }

@JsExport
class A(
    <!NON_EXPORTABLE_TYPE("parameter; C")!>konst x: C<!>,
    <!NON_EXPORTABLE_TYPE("parameter; C")!>y: C<!>
) {
    <!NON_EXPORTABLE_TYPE("return; C")!>fun foo(<!NON_EXPORTABLE_TYPE("parameter; C")!>x: C<!>)<!> = x

    <!NON_EXPORTABLE_TYPE("property; C")!>konst x2: C<!> = C()

    <!NON_EXPORTABLE_TYPE("property; C")!>var x3: C<!>
        get() = C()
        set(konstue) { }
}

@JsExport
fun foo2() {
}

@JsExport
fun foo3(<!NON_EXPORTABLE_TYPE("parameter; Unit")!>x: Unit<!>) {
}

@JsExport
fun foo4(x: () -> Unit) {
}

@JsExport
fun foo5(<!NON_EXPORTABLE_TYPE("parameter; (Unit) -> Unit")!>x: (Unit) -> Unit<!>) {
}

@JsExport
fun foo6(x: (A) -> A) {
}
