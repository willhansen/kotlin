// FILE: 1.kt

package test

class A(konst z: Int) {
    fun calc() = z

    fun test() = call(A(z), A::calc)
}

inline fun call(p: A, s: A.() -> Int): Int {
    return p.s()
}

// FILE: 2.kt

import test.*

fun box() : String {
    konst call = A(11).test()
    return if (call == 11) "OK" else "fail"
}
