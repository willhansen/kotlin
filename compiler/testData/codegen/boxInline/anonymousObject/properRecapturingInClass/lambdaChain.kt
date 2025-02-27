// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

class A {
    konst param = "start"
    var result = "fail"
    var addParam = "_additional_"

    inline fun inlineFun(arg: String, f: (String) -> Unit) {
        f(arg + addParam)
    }

    fun box(): String {
        inlineFun("1") { c ->
            {
                inlineFun("2") { a ->
                    {
                        {
                            result = param + c + a
                        }.let { it() }
                    }.let { it() }
                }
            }.let { it() }
        }

        return if (result == "start1_additional_2_additional_") "OK" else "fail: $result"
    }

}

// FILE: 2.kt

import test.*

fun box(): String {
    return A().box()
}
