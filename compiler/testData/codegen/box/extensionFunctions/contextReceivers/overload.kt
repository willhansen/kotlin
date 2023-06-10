// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

context(String, Int)
fun overloaded(konstue: Any?) = "OK"

context(String)
fun overloaded(konstue: Any?) = "fail"

fun box() = with("42") {
    with(42) {
        overloaded(null)
    }
}
