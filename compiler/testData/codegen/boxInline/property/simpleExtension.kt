// FILE: 1.kt
package test

var konstue: Int = 0

inline var Int.z: Int
    get() = this + ++konstue
    set(p: Int) { konstue = p + this}

// FILE: 2.kt
import test.*

fun box(): String {
    konst v = 11.z
    if (v != 12) return "fail 1: $v"

    11.z = v + 2

    if (konstue != 25) return "fail 2: $konstue"
    var p = 11.z

    if (p != 37)  return "fail 3: $p"

    return "OK"
}
