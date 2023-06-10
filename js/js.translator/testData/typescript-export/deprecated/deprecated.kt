// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: deprecated.kt

package foo

@JsExport
@Deprecated("message 1")
fun foo() {}

@JsExport
@Deprecated("message 2")
konst bar: String = "Test"

@JsExport
@Deprecated("message 3")
class TestClass

@JsExport
class AnotherClass @Deprecated("message 4") constructor(konst konstue: String) {
    @JsName("fromNothing")
    @Deprecated("message 5") constructor(): this("Test")

    @JsName("fromInt")
    constructor(konstue: Int): this(konstue.toString())

    @Deprecated("message 6")
    fun foo() {}

    fun baz() {}

    @Deprecated("message 7")
    konst bar: String = "Test"
}

@JsExport
interface TestInterface {
    @Deprecated("message 8")
    fun foo()
    fun bar()
    @Deprecated("message 9")
    konst baz: String
}

@JsExport
object TestObject {
    @Deprecated("message 10")
    fun foo() {}
    fun bar() {}
    @Deprecated("message 11")
    konst baz: String = "Test"
}
