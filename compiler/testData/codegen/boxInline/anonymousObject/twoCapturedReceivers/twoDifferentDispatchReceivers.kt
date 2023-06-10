// NO_CHECK_LAMBDA_INLINING
// IGNORE_BACKEND: JS
// FILE: 1.kt

package test

class Company(konst name: String) {
    fun sayName() = Person("test").doSayName { name }
}

class Person(konst name: String) {

    inline fun doSayName(crossinline call: () -> String): String {
        return companyName { parsonName { call() } }
    }

    inline fun parsonName(call: () -> String) = call()

    fun companyName(call: () -> String) = call()

}

// FILE: 2.kt

import test.*

fun box(): String {
    return Company("OK").sayName()
}
