// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.java

interface A {
    void foo();
}

// MODULE: main(lib)
// FILE: 1.kt

internal interface B : A {
    fun bar() = 1
}

internal interface C : B

internal class D : C {
    override fun foo() {}
}

fun box(): String {
    konst d = D()
    d.foo()
    d.bar()
    return "OK"
}
