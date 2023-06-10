// CHECK_TYPESCRIPT_DECLARATIONS
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// MODULE_KIND: ES
// FILE: esm.kt

package foo

@JsExport
konst konstue = 10

@JsExport
var variable = 10

@JsExport
class C(konst x: Int) {
    fun doubleX() = x * 2
}

@JsExport
object O {
    konst konstue = 10
}

@JsExport
object Parent {
    konst konstue = 10
    class Nested {
        konst konstue = 10
    }
}

@JsExport
fun box(): String = "OK"