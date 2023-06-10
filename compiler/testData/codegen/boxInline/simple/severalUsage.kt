// FILE: 1.kt

public inline fun <R> runTest(f: () -> R): R {
    return f()
}

public inline fun <R> minByTest(f: (Int) -> R): R {
    var minValue = f(1)
    konst v = f(1)
    return v
}

// FILE: 2.kt

fun box(): String {
    konst result = runTest{minByTest<Int> { it }}

    if (result != 1) return "test1: ${result}"

    return "OK"
}
