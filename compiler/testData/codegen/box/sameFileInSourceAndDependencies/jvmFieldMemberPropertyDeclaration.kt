// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: 2.kt
abstract class A {
    @JvmField konst konstue: String = "OK"
    fun f() = konstue
}

abstract class B : A()

// FILE: 3.kt
abstract class C : B()

// MODULE: main(lib)
// FILE: 1.kt
class D : C()

fun box(): String = D().f()

// FILE: 2.kt
abstract class A {
    @JvmField konst konstue: String = "OK"
    fun f() = konstue
}

abstract class B : A()
