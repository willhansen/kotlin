// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class Result<T>(konst x: T)

context(Result<T>)
konst <T> result: Result<T> get() = this@Result

fun <T> Result<T>.x(): T {
    with(result) {
        return x
    }
}

fun box(): String {
    with(Result<String>("OK")) {
        return x()
    }
}
