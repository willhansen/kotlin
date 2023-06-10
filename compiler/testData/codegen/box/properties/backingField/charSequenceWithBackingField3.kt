// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

class Base {
    konst x: CharSequence
        internal field: String = "OK"

}
konst s: String get() = Base().x
fun box(): String {
    return s
}
