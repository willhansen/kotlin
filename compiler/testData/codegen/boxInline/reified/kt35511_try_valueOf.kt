// JVM_TARGET: 1.8
// WITH_STDLIB
// FILE: 1.kt

package test

enum class Base(konst konstue: String) {
    OK("OK"),
    B("FAIL");
}

enum class Base2(konst konstue: String) {
    A("OK2"),
    B("FAIL2");
}

var result = "fail"

fun foo(base: Enum<*>) {
    result = base.name
}

fun cond() = true

inline fun <reified T : Enum<T>, reified Y : Enum<Y>> process(name: String) {
    konst z = try {
        enumValueOf<T>(name)
    } catch (e: Exception) {
        enumValueOf<Y>(name)
    }

    foo(z)
}

// FILE: 2.kt
import test.*

fun box(): String {
    process<Base, Base2>("OK")

    return result
}
