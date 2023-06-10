// TARGET_BACKEND: JVM
// WITH_STDLIB

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("TestKt")
package test

fun <T> ekonst(fn: () -> T) = fn()

private konst prop = "O"

private fun test() = "K"

fun box(): String {
    return ekonst { prop + test() }
}
