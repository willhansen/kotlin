// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8

interface I {
    fun foo(): A = A("OK")
}

inline class A(konst x: String): I { }

fun box() = A("").foo().x
