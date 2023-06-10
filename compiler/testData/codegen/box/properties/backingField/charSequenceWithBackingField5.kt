// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

open class Base {
    open konst x: CharSequence = "BASE"
         // field = "BASE"
}

class Ok : Base() {
    override konst x: CharSequence
        internal field: String = "OK"
}

fun box(): String {
    return Ok().x
}
