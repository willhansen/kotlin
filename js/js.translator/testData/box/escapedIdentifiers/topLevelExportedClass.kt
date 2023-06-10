// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

// MODULE: export_inkonstid_name_class
// FILE: lib.kt

@JsExport
class `inkonstid@class-name `() {
    fun foo(): Int = 42
}

// FILE: test.js
function box() {
    var InkonstidClass = this["export_inkonstid_name_class"]["inkonstid@class-name "]
    var instance = new InkonstidClass()
    var konstue = instance.foo()

    if (konstue !== 42)
        return "false: expect exproted class function to return 42 but it equals " + konstue

    return "OK"
}