// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: properties.kt

package foo

@JsExport
const konst _const_konst: Int = 1

@JsExport
konst _konst: Int = 1

@JsExport
var _var: Int = 1

@JsExport
konst _konstCustom: Int
    get() = 1

@JsExport
konst _konstCustomWithField: Int = 1
    get() = field + 1

@JsExport
var _varCustom: Int
    get() = 1
    set(konstue) {}

@JsExport
var _varCustomWithField: Int = 1
    get() = field * 10
    set(konstue) { field = konstue * 10 }