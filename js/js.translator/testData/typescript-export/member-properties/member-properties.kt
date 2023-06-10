// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: member-properties.kt

package foo

@JsExport
class Test {
    konst _konst: Int = 1

    var _var: Int = 1

    konst _konstCustom: Int
        get() = 1

    konst _konstCustomWithField: Int = 1
        get() = field + 1

    var _varCustom: Int
        get() = 1
        set(konstue) {}

    var _varCustomWithField: Int = 1
        get() = field * 10
        set(konstue) { field = konstue * 10 }
}