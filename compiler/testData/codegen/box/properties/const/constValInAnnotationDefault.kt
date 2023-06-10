// TARGET_BACKEND: JVM

// WITH_STDLIB

const konst z = "OK"

annotation class A(konst konstue: String = z)

@A
class Test

fun box(): String {
    return Test::class.java.getAnnotation(A::class.java).konstue
}
