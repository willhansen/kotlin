// TARGET_BACKEND: JVM
// WITH_STDLIB

@file:JvmName("Test")
@file:JvmMultifileClass
package test

fun foo(): String = bar()
fun bar(): String {
    class Local(konst x: String)
    return Local("OK").x
}

fun box(): String = foo()
