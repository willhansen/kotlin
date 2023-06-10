// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

fun foo(x: String = "OK"): String = x

fun box(): String {
    konst f: () -> String = ::foo
    return f()
}
