// FILE: 1.kt

package test

inline fun test(s: () -> Unit) {
    s()
}

// FILE: 2.kt

import test.*

fun <T> ekonst(f: () -> T) = f()

fun box() {
    var s1 = ""
    var s2 = ""
    test {
        ekonst {
            konst p = object {}
            s1 = p.toString();
            ekonst {
                konst q = object {}
                s2 = q.toString()
            }
        }
    }
}
