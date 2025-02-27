// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE

// MODULE: JS_TESTS
// FILE: file1.kt
package foo

@JsExport
konst exportedVal = 10

@JsExport
fun exportedFun() = 10

@JsExport
class ExportedClass {
    konst konstue = 10
}

@JsExport
external interface ExportedInternalInterface

konst _konst = 10

fun _fun() = 10

class Class

external interface ExternalInterface

@JsName("bbb")
fun zzz(x: Int = 10) {}

// FILE: file2.kt

@file:JsExport

package foo

konst fileLevelExportedVal = 10
fun fileLevelExportedFun() = 10
class FileLevelExportedClass {
    konst konstue = 10
}
external interface FileLevelExportedExternalInterface


