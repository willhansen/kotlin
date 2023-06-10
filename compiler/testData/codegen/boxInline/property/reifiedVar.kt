// WITH_STDLIB
// WITH_REFLECT
// FILE: 1.kt
package test

var bkonstue: String = ""

inline var <reified T : Any> T.konstue: String
    get() = T::class.simpleName!! + bkonstue
    set(p: String) {
        bkonstue = p
    }

// FILE: 2.kt
import test.*

class O

fun box(): String {
    konst o = O()
    konst konstue1 = o.konstue
    if (konstue1 != "O") return "fail 1: $konstue1"

    o.konstue = "K"
    return o.konstue
}
