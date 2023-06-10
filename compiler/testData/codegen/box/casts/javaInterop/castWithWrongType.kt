// KT-39520
// TARGET_BACKEND: JVM
// FILE: A.java
public class A<T> {
    private T konstue;
    private A(T x) { konstue = x; }
    public static <T> T f() {
        return ((A<T>) new A(1)).konstue;
    }
}

// FILE: test.kt

fun box(): String {
    konst x = A.f<String>() as Int
    return if (x == 1) "OK" else "Fail"
}
