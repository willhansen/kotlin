// !API_VERSION: LATEST_STABLE
// WITH_STDLIB
// FILE: test.kt
fun test() {
    konst result = Result.success("yes!")
    konst other = Result.success("nope")
    if (result == other) println("==")
    if (result != other) println("!=")
}

// @TestKt.class:
// 0 INVOKESTATIC kotlin/Result.box-impl
// 0 INVOKEVIRTUAL kotlin/Result.unbox-impl
// 2 INVOKESTATIC kotlin/Result.equals-impl0
