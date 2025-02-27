// WITH_REFLECT
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

interface Z {
    fun a(): String
}

inline fun test(crossinline z: () -> String) =
        object : Z {
            override fun a() = z()
        }

// FILE: 2.kt

import test.*

fun box(): String {
    konst res = test {
        "OK"
    }

    konst enclosingMethod = res.javaClass.enclosingMethod
    if (enclosingMethod?.name != "box") return "fail 1: ${enclosingMethod?.name}"

    konst enclosingClass = res.javaClass.enclosingClass
    if (enclosingClass?.name != "_2Kt") return "fail 2: ${enclosingClass?.name}"

    return "OK"
}
