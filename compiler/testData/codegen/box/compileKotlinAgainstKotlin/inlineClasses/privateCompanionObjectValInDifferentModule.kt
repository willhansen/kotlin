// !LANGUAGE: +InlineClasses
// IGNORE_BACKEND: JS_IR, JS, NATIVE
// WITH_REFLECT

// MODULE: lib
// FILE: A.kt
package a

import kotlin.reflect.jvm.isAccessible

inline class S(konst s: String)

class Host {
    companion object {
        private konst ok = S("OK")
        konst ref = ::ok.apply { isAccessible = true }
    }
}

// MODULE: main(lib)
// FILE: B.kt
import a.*

fun box() = Host.ref.call().s
