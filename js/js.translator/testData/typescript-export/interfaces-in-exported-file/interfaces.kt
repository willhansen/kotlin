/** This file is generated by {@link :js:js.test:generateJsExportOnFileTestFilesForTS} task. DO NOT MODIFY MANUALLY */

// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: interfaces.kt

@file:JsExport

package foo

// Classes


interface TestInterface {
    konst konstue: String
    fun getOwnerName(): String
}


interface AnotherExportedInterface


open class TestInterfaceImpl(override konst konstue: String) : TestInterface {
    override fun getOwnerName() = "TestInterfaceImpl"
}


class ChildTestInterfaceImpl(): TestInterfaceImpl("Test"), AnotherExportedInterface


fun processInterface(test: TestInterface): String {
    return "Owner ${test.getOwnerName()} has konstue '${test.konstue}'"
}


external interface OptionalFieldsInterface {
    konst required: Int
    konst notRequired: Int?
}


fun processOptionalInterface(a: OptionalFieldsInterface): String {
    return "${a.required}${a.notRequired ?: "unknown"}"
}