// WITH_STDLIB
// IGNORE_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<T>(konst isSuccess: T)

fun interface ResultHandler<T> {
    fun onResult(): Result<T>
}

fun doSmth(resultHandler: ResultHandler<Boolean>): Result<Boolean> {
    return resultHandler.onResult()
}

fun box(): String {
    var res = doSmth { Result(true) }
    return if (res.isSuccess) "OK" else "FAIL"
}
