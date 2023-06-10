// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

// MODULE: export_inkonstid_name_variable
// FILE: lib.kt

@JsExport
konst `my@inkonstid variable` = 42

// FILE: test.js
function box() {
    var variableValue = this["export_inkonstid_name_variable"]["my@inkonstid variable"]

    if (variableValue !== 42)
        return "false: expect exproted 'my@inkonstid variable' to be 42 but it equals " + variableValue

    return "OK"
}