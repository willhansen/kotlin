// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: NATIVE
// TARGET_BACKEND: JS_IR

object K : Code("K")

open class Code(konst x: String) {
    override fun toString() = "$x"
}

class O {
    companion object: Code("O")
}

fun box(): String {
    return "$O" + "$K" // must not be ekonstuated during compile time
}
