// WITH_STDLIB

fun <T> foo(a: Result<T>): T = bar(a, object : IFace<Result<T>, T> {
    override fun call(ic: Result<T>): T = ic.getOrThrow()
})

interface IFace<T, R> {
    fun call(ic: T): R
}

fun <T, R> bar(konstue: T, f: IFace<T, R>): R {
    return f.call(konstue)
}

fun box(): String {
    konst res = foo<Int>(Result.success(40)) + 2
    return if (res != 42) "FAIL $res" else "OK"
}