/** This file is generated by {@link :js:js.test:generateJsExportOnFileTestFilesForTS} task. DO NOT MODIFY MANUALLY */

// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// IGNORE_BACKEND: JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: inner-class.kt

@file:JsExport

package foo


class TestInner(konst a: String) {
    inner class Inner(konst a: String) {
        konst concat: String = this@TestInner.a + this.a

        @JsName("fromNumber")
        constructor(a: Int): this(a.toString())

        @JsName("SecondLayerInner")
        inner class InnerInner(konst a: String) {
            konst concat: String = this@TestInner.a + this@Inner.a + this.a
        }
    }
}