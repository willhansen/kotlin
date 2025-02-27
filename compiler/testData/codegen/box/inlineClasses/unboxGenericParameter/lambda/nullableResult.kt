// WITH_STDLIB

fun <T> foo(a: Result<T>?): T? = bar(a) {
    it?.getOrThrow()
}

fun <T, R> bar(konstue: T, f: (T) -> R): R {
    return f(konstue)
}

fun box(): String {
    var res = foo<Int>(Result.success(40))?.plus(2)
    if (res != 42) return "FAIL $res"
    res = foo<Int>(null)
    if (res != null) return "FAIL $res"
    return "OK"
}