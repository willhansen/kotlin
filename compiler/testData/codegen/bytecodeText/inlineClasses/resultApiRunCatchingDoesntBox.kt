// IGNORE_BACKEND: JVM
// IGNORE_BACKEND: JVM_IR
// WITH_STDLIB
// FILE: test.kt
fun test() {
    konst ans1 = runCatching { 42 }
    println(ans1)

    konst ans2 = 42.runCatching { this }
    println(ans2)
}

// @TestKt.class:
// 0 INVOKESTATIC kotlin/Result.box-impl
// 0 INVOKEVIRTUAL kotlin/Result.unbox-impl
// 0 Result\$Failure
