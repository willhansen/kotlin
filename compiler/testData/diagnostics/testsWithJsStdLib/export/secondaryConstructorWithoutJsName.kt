// FIR_IDENTICAL
// !OPT_IN: kotlin.js.ExperimentalJsExport
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

@JsExport
class C(konst x: String) {
    <!WRONG_EXPORTED_DECLARATION("secondary constructor without @JsName")!>constructor(x: Int)<!>: this(x.toString())
}

@JsExport
class C2(konst x: String) {
    @JsName("JsNameProvided")
    constructor(x: Int): this(x.toString())
}

@JsExport
class C3(konst x: String) {
    protected <!WRONG_EXPORTED_DECLARATION("secondary constructor without @JsName")!>constructor(x: Int)<!>: this(x.toString())
}
