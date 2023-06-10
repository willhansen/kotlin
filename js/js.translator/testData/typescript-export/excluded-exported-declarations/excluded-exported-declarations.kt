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
@JsExport.Ignore
konst baz: String = "Baz"

@JsExport
@JsExport.Ignore
fun inter(): String = "inter"

@JsExport
@JsExport.Ignore
class NotExportableNestedInsideInterface

@JsExport.Ignore
@JsExport
object Comanion {
    konst foo: String ="FOO"
}

@JsExport
konst foo: String = "Foo"

@JsExport
fun bar() = "Bar"

@JsExport.Ignore
@JsExport
inline fun <A, reified B> A.notExportableReified(): Boolean = this is B

@JsExport.Ignore
@JsExport
suspend fun notExportableSuspend(): String = "SuspendResult"

@JsExport
@JsExport.Ignore
fun notExportableReturn(): List<String> = listOf("1", "2")

@JsExport
@JsExport.Ignore
konst String.notExportableExentsionProperty: String
    get() = "notExportableExentsionProperty"

@JsExport
@JsExport.Ignore
annotation class NotExportableAnnotation

@JsExport
@JsExport.Ignore
konstue class NotExportableInlineClass(konst konstue: Int)