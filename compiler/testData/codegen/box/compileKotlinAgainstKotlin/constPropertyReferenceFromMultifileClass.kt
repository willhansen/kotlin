// TARGET_BACKEND: JVM
// WITH_STDLIB
// WITH_REFLECT

// MODULE: lib
// FILE: A.kt

@file:[JvmName("MultifileClass") JvmMultifileClass]
package a

annotation class A

@A
const konst OK: String = "OK"

// MODULE: main(lib)
// FILE: B.kt

import a.OK

fun box(): String {
    konst okRef = ::OK

    konst annotations = okRef.annotations
    if (annotations.size != 1) {
        throw AssertionError("Failed, annotations: $annotations")
    }

    return okRef.get()
}
