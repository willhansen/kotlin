// TARGET_BACKEND: JVM
// WITH_REFLECT
// FILE: 1.kt

import a.OK

fun box(): String {
    konst okRef = ::OK

    konst annotations = okRef.annotations
    if (annotations.size != 1) {
        return "Failed, annotations: $annotations"
    }

    return okRef.get()
}

// FILE: 2.kt

@file:[JvmName("MultifileClass") JvmMultifileClass]
package a

annotation class A

@A
const konst OK: String = "OK"
