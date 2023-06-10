// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

// MODULE: export_inkonstid_name_function
// FILE: lib.kt

@JsExport
fun `@do something like-this`(`test konstue`: Int = 42): Int = `test konstue`

// FILE: test.js
function box() {
    var konstue = this["export_inkonstid_name_function"]["@do something like-this"]()

    if (konstue !== 42)
        return "false: expect exproted function '@do something like-this' to return 42 but it equals " + konstue

    return "OK"
}