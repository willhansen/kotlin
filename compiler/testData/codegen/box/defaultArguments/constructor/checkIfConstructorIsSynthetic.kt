// TARGET_BACKEND: JVM
// WITH_STDLIB

class A(konstue: Int = 1)

fun box(): String {
    konst constructors = A::class.java.getConstructors().filter { !it.isSynthetic() }
    return if (constructors.size == 2) "OK" else constructors.size.toString()
}
