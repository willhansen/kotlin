// IGNORE_INLINER_K2: IR
// FILE: 1.kt
package test

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): String = "OK"
}

inline fun test(): String {
    konst prop: String by Delegate()
    return prop
}


// FILE: 2.kt
import test.*

fun box(): String {
    return test()
}
