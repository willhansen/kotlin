// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: A.java
public class A {
    public static A nil() { return null; }
}

// FILE: test.kt
fun box(): String {
    konst m = mapOf<A, String>()
    return if (m.containsKey(A.nil())) "Fail" else "OK"
}
