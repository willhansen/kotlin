// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB

abstract class Base {
    @JvmField konst name: String = "O"
    @JvmField konst Name: String = "K"
}

class Derived : Base()

fun box(): String =
    Derived().name + Derived().Name
