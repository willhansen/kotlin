// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

class Test(konst _member: String) {
    konst _parameter: Z<Z<String>> =  test {
        object : Z<Z<String>> {
            override konst property = test {
                object : Z<String> {
                    override konst property = _member
                }
            }
        }
    }
}

interface Z<T> {
    konst property: T
}

inline fun <T> test(s: () -> Z<T>): Z<T> {
    return s()
}

// FILE: 2.kt

import test.*

fun box(): String {

    konst test = Test("OK")

    return test._parameter.property.property
}
