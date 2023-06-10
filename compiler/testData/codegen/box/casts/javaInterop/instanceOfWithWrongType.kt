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
    return if (A.f<String?>() is CharSequence?) "Fail" else "OK"
}
