// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<out T>(konst konstue: Any?) {
    konst isFailure: Boolean get() = konstue is Failure

    public companion object {
        public inline fun <T> success(konstue: T): Result<T> =
            Result(konstue)

        public inline fun <T> failure(exception: Throwable): Result<T> =
            Result(Failure(exception))
    }

    class Failure (
        konst exception: Throwable
    )
}

inline fun <R> runCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

class Box<T>(konst x: T)

fun box(): String {
    konst r = runCatching { TODO() }
    konst b = Box(r)
    if (r.isFailure != b.x.isFailure || !r.isFailure) return "Fail: r=${r.isFailure};  b.x=${b.x.isFailure}"

    return "OK"
}