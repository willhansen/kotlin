// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.java

class A {
    public A(double x, int y) { }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst constructor = ::A
    constructor(0.0, 0)
    return "OK"
}
