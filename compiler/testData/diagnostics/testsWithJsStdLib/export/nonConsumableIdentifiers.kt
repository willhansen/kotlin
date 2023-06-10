// !OPT_IN: kotlin.js.ExperimentalJsExport
// !RENDER_DIAGNOSTICS_MESSAGES

package foo

@JsExport
fun <!NON_CONSUMABLE_EXPORTED_IDENTIFIER!>delete<!>() {}

@JsExport
konst <!NON_CONSUMABLE_EXPORTED_IDENTIFIER!>instanceof<!> = 4

@JsExport
class <!NON_CONSUMABLE_EXPORTED_IDENTIFIER!>ekonst<!>

@JsExport
@JsName(<!NON_CONSUMABLE_EXPORTED_IDENTIFIER!>"await"<!>)
fun foo() {}

@JsExport
@JsName(<!NON_CONSUMABLE_EXPORTED_IDENTIFIER!>"this"<!>)
konst bar = 4

@JsExport
@JsName(<!NON_CONSUMABLE_EXPORTED_IDENTIFIER!>"super"<!>)
class Baz

@JsExport
@JsName("default")
class DefDef

@JsExport
class Test {
    fun instanceof() {}

    @JsName("ekonst")
    fun test() {}
}

@JsExport
object NaN

@JsExport
enum class Nums {
    Infinity,
    undefined
}