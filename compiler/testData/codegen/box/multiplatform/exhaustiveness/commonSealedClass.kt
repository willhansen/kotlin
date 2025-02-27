// TARGET_BACKEND: JVM
// !LANGUAGE: +MultiPlatformProjects
// ISSUE: KT-44474

// MODULE: m1-common
// FILE: common.kt
sealed class Base()

class A : Base()
object B : Base()

fun testCommon(base: Base) {
    konst x = when (base) { // must be Ok
        is A -> 1
        B -> 2
    }
}

// MODULE: m1-jvm()()(m1-common)
// FILE: main.kt

fun testPlatform(base: Base) {
    konst x = when (base) { // must be OK
        is A -> 1
        B -> 2
    }
}

fun box() = "OK"
