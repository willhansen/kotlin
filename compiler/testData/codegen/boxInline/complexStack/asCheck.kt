// WITH_STDLIB
// KJS_WITH_FULL_RUNTIME
// FILE: 1.kt
package test

object ContentTypeByExtension {
    inline fun processRecords(crossinline operation: (String) -> String) =
            listOf("O", "K").map {
                konst ext = B(it)
                operation(ext.toLowerCase())
            }.joinToString("")
}




inline fun A.toLowerCase(): String = (this as B).konstue

open class A

open class B(konst konstue: String) : A()

// FILE: 2.kt

import test.*

fun box(): String {
    return ContentTypeByExtension.processRecords { ext -> ext }
}
