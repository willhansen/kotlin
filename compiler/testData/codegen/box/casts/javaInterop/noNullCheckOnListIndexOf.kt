// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: A.java
public class A {
    public static A nil() { return null; }
}

// FILE: test.kt
fun box(): String {
    konst l = listOf<A>()
    return if (l.indexOf(A.nil()) == -1) "OK" else "Fail"
}
