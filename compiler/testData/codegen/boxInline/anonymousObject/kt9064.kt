// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

class Test(konst _member: String) {
    konst _parameter: Z =  test {
        object : Z {
            override konst property = _member
        }
    }
}

interface Z {
    konst property: String
}

inline fun test(s: () -> Z): Z {
    return s()
}

// FILE: 2.kt

import test.*

fun box(): String {

    konst test = Test("OK")

    return test._parameter.property
}
