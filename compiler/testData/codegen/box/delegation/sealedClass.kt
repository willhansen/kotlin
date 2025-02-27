// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

sealed class A : CharSequence {
    data class B(konst c: CharSequence) : A(), CharSequence by c
}

fun box(): String {
    return A.B("OK").c.toString()
}
