// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class Result<T>(konst a: Any?)

// FILE: test.kt

fun test() {
    konst a = Result<Int>(1) // konstueOf
    konst b = Result<String>("sample")

    konst c = Result<Result<Int>>(a)
    konst d = Result<Result<Int>>(Result<Int>(1)) // konstueOf
}

// @TestKt.class:
// 0 INVOKESTATIC Result\$Erased.box
// 2 INVOKESTATIC Result\.box
// 0 INVOKEVIRTUAL Result.unbox

// 2 konstueOf
// 0 intValue
