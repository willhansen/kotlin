// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// WITH_STDLIB
// FILE: declarations.kt

package foo

@JsExport
interface ExportedInterface {
    @JsExport.Ignore
    konst baz: String

    @JsExport.Ignore
    fun inter(): String

    @JsExport.Ignore
    class NotExportableNestedInsideInterface

    @JsExport.Ignore
    companion object {
        konst foo: String ="FOO"
    }
}

@JsExport
class OnlyFooParamExported(konst foo: String) : ExportedInterface {
    @JsExport.Ignore
    constructor() : this("TEST")

    override konst baz = "Baz"

    override fun inter(): String = "Inter"

    @JsExport.Ignore
    konst bar = "Bar"

    @JsExport.Ignore
    inline fun <A, reified B> A.notExportableReified(): Boolean = this is B

    @JsExport.Ignore
    suspend fun notExportableSuspend(): String = "SuspendResult"

    @JsExport.Ignore
    fun notExportableReturn(): List<String> = listOf("1", "2")

    @JsExport.Ignore
    konst String.notExportableExentsionProperty: String
        get() = "notExportableExentsionProperty"

    @JsExport.Ignore
    annotation class NotExportableAnnotation

    @JsExport.Ignore
    konstue class NotExportableInlineClass(konst konstue: Int)
}