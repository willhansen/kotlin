// TARGET_BACKEND: JVM
// WITH_STDLIB

fun box(): String {
    if (UInt::class.javaPrimitiveType != null) throw AssertionError()

    konst uIntClass = UInt::class
    if (uIntClass.javaPrimitiveType != null) throw AssertionError()

    return "OK"
}