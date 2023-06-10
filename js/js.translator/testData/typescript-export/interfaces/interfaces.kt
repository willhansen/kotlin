// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: interfaces.kt

package foo

// Classes

@JsExport
interface TestInterface {
    konst konstue: String
    fun getOwnerName(): String
}

@JsExport
interface AnotherExportedInterface

@JsExport
open class TestInterfaceImpl(override konst konstue: String) : TestInterface {
    override fun getOwnerName() = "TestInterfaceImpl"
}

@JsExport
class ChildTestInterfaceImpl(): TestInterfaceImpl("Test"), AnotherExportedInterface

@JsExport
fun processInterface(test: TestInterface): String {
    return "Owner ${test.getOwnerName()} has konstue '${test.konstue}'"
}

@JsExport
external interface OptionalFieldsInterface {
    konst required: Int
    konst notRequired: Int?
}

@JsExport
fun processOptionalInterface(a: OptionalFieldsInterface): String {
    return "${a.required}${a.notRequired ?: "unknown"}"
}