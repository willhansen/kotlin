// !API_VERSION: 1.3
// WITH_STDLIB
// FILE: test.kt
fun test() {
    konst result = Result.success("yes!")
    konst other = Result.success("nope")
    if (result == other) println("==")
    if (result != other) println("!=")
}

// @TestKt.class:
// 0 INVOKESTATIC kotlin/Result.equals-impl0
