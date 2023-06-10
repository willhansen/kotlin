// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: enum-classes.kt

package foo

@JsExport
enum class TestEnumClass(konst constructorParameter: String) {
    A("aConstructorParameter"),
    B("bConstructorParameter");

    konst foo = ordinal

    fun bar(konstue: String) = konstue

    fun bay() = name

    class Nested {
        konst prop: String = "hello2"
    }
}

@JsExport
class OuterClass {
    enum class NestedEnum {
        A,
        B
    }
}

