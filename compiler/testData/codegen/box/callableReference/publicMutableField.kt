// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.java

public class A {
    public int field = 239;
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst a = A()
    konst f = A::field
    if (f.get(a) != 239) return "Fail 1: ${f.get(a)}"
    f.set(a, 42)
    if (f.get(a) != 42) return "Fail 2: ${f.get(a)}"
    if (f.get(a) != 42) return "Fail 2: ${f.get(a)}"
    return "OK"
}
