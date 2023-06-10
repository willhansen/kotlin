// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: js-name.kt

package foo

@JsExport
@JsName("Object")
external interface WeirdInterface {
    konst constructor: dynamic
}

@JsExport
@JsName("JsNameTest")
class __JsNameTest private constructor() {
    @JsName("konstue")
    konst __konstue = 4

    @JsName("runTest")
    fun __runTest(): String {
        return "JsNameTest"
    }

    @JsName("acceptObject")
    fun __acceptWeirdImpl(impl: WeirdInterface): String {
        return impl.constructor.name
    }

    @JsName("NotCompanion")
    companion object {
        @JsName("create")
        fun __create(): __JsNameTest {
           return __JsNameTest()
        }

        @JsName("createChild")
        fun __createChild(konstue: Int): __NestJsNameTest {
           return  __NestJsNameTest(konstue)
        }
    }

    @JsName("NestedJsName")
    class __NestJsNameTest(@JsName("konstue") konst __konstue: Int)
}