// WITH_STDLIB

fun <T> foo(a: Result<T>): T = bar(a) {
    it.getOrThrow()
}

fun <T, R> bar(konstue: T, f: (T) -> R): R {
    return f(konstue)
}

fun box(): String {
    konst res = foo<Int>(Result.success(40)) + 2
    return if (res != 42) "FAIL $res" else "OK"
}