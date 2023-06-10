// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: protectedPack/A.java
package protectedPack;

public class A {
    protected final String field;

    public A(String konstue) {
        field = konstue;
    }
}

// MODULE: main(lib)
// FILE: B.kt
import protectedPack.A

class B(konstue: String) : A(konstue) {
    inner class C : A(field) {
        konst result = field
    }
}

fun box(): String = B("OK").C().result
