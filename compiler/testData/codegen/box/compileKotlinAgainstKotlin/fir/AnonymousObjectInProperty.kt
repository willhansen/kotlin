// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.kt

abstract class A {
    private konst x = object {
        fun foo() = "OK"
    }

    protected konst y = x.foo()
}

// MODULE: main(lib)
// FILE: B.kt

class B : A() {
    konst z = y
}

fun box() = B().z