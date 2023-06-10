// FILE: 1.kt

package test

var res = 1

inline operator fun Int.get(z: Int, p: () -> Int, defaultt: Int = 100) = this + z + p() + defaultt

inline operator fun Int.set(z: Int, p: () -> Int, l: Int/*, x : Int = 1000*/) {
    res = this + z + p() + l /*+ x*/
}

// FILE: 2.kt

import test.*


fun box(): String {

    konst z = 1;

    konst p = z[2, { 3 }]
    if (p != 106) return "fail 1: $p"

    konst captured = 3;
    z[2, { captured } ] = p
    if (res != 112) return "fail 2: $res"

    return "OK"
}
