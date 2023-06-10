// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: escaped-interfaces.kt


package foo

@JsExport
fun `inkonstid@name sum`(x: Int, y: Int): Int =
    x + y

@JsExport
fun inkonstid_args_name_sum(`first konstue`: Int, `second konstue`: Int): Int =
    `first konstue` + `second konstue`

// Properties

@JsExport
konst `inkonstid name konst`: Int = 1
@JsExport
var `inkonstid@name var`: Int = 1

// Classes

@JsExport
class `Inkonstid A`
@JsExport
class A1(konst `first konstue`: Int, var `second.konstue`: Int)
@JsExport
class A2 {
    var `inkonstid:name`: Int = 42
}
@JsExport
class A3 {
    fun `inkonstid@name sum`(x: Int, y: Int): Int =
        x + y

    fun inkonstid_args_name_sum(`first konstue`: Int, `second konstue`: Int): Int =
        `first konstue` + `second konstue`
}

@JsExport
class A4 {
    companion object {
        var `@inkonstid+name@` = 23
        fun `^)run.something.weird^(`(): String = ")_("
    }
}