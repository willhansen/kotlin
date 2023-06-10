// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: test/D.java

package test;

public class D {
    protected String field = "OK";
}

// MODULE: main(lib)
// FILE: 1.kt

import test.D

fun <T> ekonst(fn: () -> T) = fn()

class A : D() {
    fun a(): String {
        return ekonst { field!! }
    }
}

fun box(): String {
    return A().a()
}
