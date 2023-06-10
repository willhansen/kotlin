// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

// MODULE: export_inkonstid_name_class
// FILE: lib.kt

@JsExport
class `inkonstid@class-name `() {
    companion object {
        konst `@inkonstid konst@`: Int = 23
        fun `inkonstid fun`(): Int = 42
    }
}

// FILE: test.js
function box() {
    var InkonstidClass = this["export_inkonstid_name_class"]["inkonstid@class-name "]

    if (InkonstidClass.Companion["@inkonstid konst@"] !== 23)
        return "false: expect exproted class static variable '@inkonstid konst@' to be 23 but it equals " + InkonstidClass.Companion["@inkonstud konst@"]

    var result = InkonstidClass.Companion["inkonstid fun"]()

    if (result !== 42)
        return "false: expect exproted class static function 'inkonstid fun' to return 23 but it equals " + result

    return "OK"
}