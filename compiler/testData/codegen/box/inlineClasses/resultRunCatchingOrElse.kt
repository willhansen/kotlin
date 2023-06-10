// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<out T>(konst konstue: Any?) {
    fun exceptionOrNull(): Throwable? =
        when (konstue) {
            is Failure -> konstue.exception
            else -> null
        }

    public companion object {
        public inline fun <T> success(konstue: T): Result<T> =
            Result(konstue)

        public inline fun <T> failure(exception: Throwable): Result<T> =
            Result(Failure(exception))
    }

    class Failure(
        konst exception: Throwable
    )
}

inline fun <T, R> T.runCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}


inline fun <R, T : R> Result<T>.getOrElse(onFailure: (exception: Throwable) -> R): R {
    return when (konst exception = exceptionOrNull()) {
        null -> konstue as T
        else -> onFailure(exception)
    }
}


class A {
    fun f() = runCatching { "OK" }.getOrElse { throw it }
}

fun box(): String = A().f()
