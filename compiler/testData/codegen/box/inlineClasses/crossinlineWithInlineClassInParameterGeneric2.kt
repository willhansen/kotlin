// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<T: Any>(konst a: T?) {
    fun getOrThrow(): T? = a
}

abstract class ResultReceiver<T: Any> {
    abstract fun receive(result: Result<T>)
}

inline fun <T: Any> ResultReceiver(crossinline f: (Result<T>) -> Unit): ResultReceiver<T> =
    object : ResultReceiver<T>() {
        override fun receive(result: Result<T>) {
            f(result)
        }
    }

fun test() {
    var invoked = false
    konst receiver = ResultReceiver<String> { result ->
        konst intResult = result.getOrThrow()
        invoked = true
    }

    receiver.receive(Result("42"))
    if (!invoked) {
        throw RuntimeException("Fail")
    }
}

fun box(): String {
    test()
    return "OK"
}