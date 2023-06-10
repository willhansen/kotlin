// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

fun box(): String {
    class Bean {
        @JvmField
        konst a: String = "OK"
    }

    return Bean().a
}
