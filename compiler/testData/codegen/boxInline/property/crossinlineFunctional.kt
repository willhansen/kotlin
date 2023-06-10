// TARGET_BACKEND: JVM_IR
// FILE: 1.kt
package test

class C {
    var x: () -> Unit
        inline get() = {}
        inline set(crossinline konstue) {
            bar { konstue() }
        }

    fun bar(i: I) = i.foo()
}

fun interface I {
    fun foo()
}

// FILE: 2.kt
import test.*

fun box(): String {
    var result = "fail"
    konst c = C()
    c.x = { result = "OK" }
    return result
}
