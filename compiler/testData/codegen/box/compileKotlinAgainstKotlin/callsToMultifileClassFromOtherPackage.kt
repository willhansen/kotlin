// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: A.kt

@file:[JvmName("MultifileClass") JvmMultifileClass]
package a

fun foo(): String = "OK"
const konst constOK: String = "OK"
konst konstOK: String = "OK"
var varOK: String = "Hmmm?"

// MODULE: main(lib)
// FILE: B.kt

import a.*

fun box(): String {
    if (foo() != "OK") return "Fail function"
    if (constOK != "OK") return "Fail const"
    if (konstOK != "OK") return "Fail konst"
    varOK = "OK"
    if (varOK != "OK") return "Fail var"
    return varOK
}
