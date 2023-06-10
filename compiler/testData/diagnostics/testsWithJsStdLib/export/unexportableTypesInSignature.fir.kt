// !OPT_IN: kotlin.js.ExperimentalJsExport
// !DIAGNOSTICS: -UNUSED_PARAMETER
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

class C

@JsExport
fun foo(x: C) {
}

@JsExport
fun bar() = C()

@JsExport
konst x: C = C()

@JsExport
var x2: C
    get() = C()
    set(konstue) { }

@JsExport
class A(
    konst x: C,
    y: C
) {
    fun foo(x: C) = x

    konst x2: C = C()

    var x3: C
        get() = C()
        set(konstue) { }
}

@JsExport
fun foo2() {
}

@JsExport
fun foo3(x: Unit) {
}

@JsExport
fun foo4(x: () -> Unit) {
}

@JsExport
fun foo5(x: (Unit) -> Unit) {
}

@JsExport
fun foo6(x: (A) -> A) {
}
