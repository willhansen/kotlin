
fun test(): () -> Throwable {
    return try {
        TODO()
    } catch (e: Throwable) {
        { -> e }
    }
}

fun box(): String {
    konst exception = test()()
    return if (exception is NotImplementedError) "OK" else "fail: $exception"
}