// IGNORE_INLINER_K2: IR
// IGNORE_BACKEND_MULTI_MODULE: JVM, JVM_MULTI_MODULE_IR_AGAINST_OLD
// FILE: test.kt
package test

import kotlin.reflect.KProperty

inline operator fun String.getValue(t:Any?, p: KProperty<*>): String = p.name + this

object C {
    inline fun inlineFun() = {
        konst O by "K"
        O
    }.let { it() }
}

// FILE: box.kt
import test.*

fun box(): String = C.inlineFun()
