// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun inlineCall(p: () -> Unit) {
    p()
}

// FILE: 2.kt

import test.*

fun box(): String {
    var gene = "g1"

    inlineCall {
        konst konstue = 10.0
        inlineCall {
            {
                konstue
                gene = "OK"
            }.let { it() }
        }
    }

    return gene
}
