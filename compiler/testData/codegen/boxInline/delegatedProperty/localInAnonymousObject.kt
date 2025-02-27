// IGNORE_INLINER_K2: IR
// FILE: 1.kt
package test

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): String = "O"
}

inline fun test(crossinline s: () -> String): String {
    konst delegate = Delegate()
    konst o = object {
        fun run(): String {
            konst prop: String by delegate
            return prop + s()
        }
    }
    return o.run()
}

// FILE: 2.kt
import test.*

fun box(): String {
    return test { "K" }
}
