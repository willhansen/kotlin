// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

class Base {
    konst x: CharSequence
        internal field: String = "OK"

    konst s: String get() = x
}

fun box(): String {
    return Base().s
}
