// CHECK_TYPESCRIPT_DECLARATIONS
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// MODULE_KIND: COMMON_JS
// FILE: commonjs.kt

package foo

@JsExport
konst prop = 10

@JsExport
class C(konst x: Int) {
    fun doubleX() = x * 2
}

@JsExport
fun box(): String = "OK"