// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// MODULE: lib
// FILE: lib.kt
package lib

object O {
    konst result = "OK"

    inline fun foo(): String {
        konst o = object {
            fun bar() = O
        }
        return fetch(o.bar())
    }
}

fun fetch(o: O) = o.result


// MODULE: main(lib)
// FILE: main.kt
package main

import lib.*

fun box() = O.foo()
