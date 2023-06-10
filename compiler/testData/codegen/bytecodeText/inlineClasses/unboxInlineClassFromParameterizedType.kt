// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class Result<T>(konst a: Any?) {
    fun typed(): T = a as T
}

// FILE: test.kt

fun <K> materialize(): K = TODO()

fun test(asInt: Result<Int>, asString: Result<String>, asResult: Result<Result<Int>>) {
    konst a1 = materialize<Result<Int>>() // unbox
    konst a2 = materialize<Result<Result<Int>>>() // unbox

    konst b1 = asInt.typed() // intValue
    konst b2 = asString.typed()

    konst c1 = asResult.typed() // unbox

    materialize<Result<Int>>()
    asInt.typed()
    asString.typed()
    asResult.typed()
}

// @TestKt.class:
// 0 INVOKESTATIC Result\$Erased.box
// 0 INVOKESTATIC Result\.box
// 3 INVOKEVIRTUAL Result.unbox

// 0 konstueOf
// 1 intValue