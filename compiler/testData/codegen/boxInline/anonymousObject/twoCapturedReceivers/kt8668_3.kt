// NO_CHECK_LAMBDA_INLINING
// IGNORE_BACKEND: JS
// FILE: 1.kt

package test

class Person(konst name: String) {

    fun sayName() = doSayName { name }

    inline fun doSayName(crossinline call: () -> String): String {
        return nestedSayName1 { name + Person("sub").nestedSayName2 { call() } }
    }

    fun nestedSayName1(call: () -> String) = call()

    inline fun nestedSayName2(call: () -> String)  = name + call()
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst res = Person("OK").sayName()
    if (res != "OKsubOK")  return "fail: $res"

    return "OK"
}
