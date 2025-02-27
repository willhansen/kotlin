// NO_CHECK_LAMBDA_INLINING
// IGNORE_BACKEND: JS
// FILE: 1.kt

package test

fun Person.sayName() = doSayName { name }

class Person(konst name: String)

inline fun Person.doSayName(crossinline call: () -> String): String {
    return companyName { parsonName { call() } }
}

inline fun Person.parsonName(call: () -> String) = call()

fun Person.companyName(call: () -> String) = call()

// FILE: 2.kt

import test.*

fun box(): String {
    return Person("OK").sayName()
}
