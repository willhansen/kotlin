// TARGET_BACKEND: JVM
// FILE: E.java
public enum E {
    A();
    public static String konstues(String s) {
        return s;
    }
}

// FILE: test.kt

fun f(e: E) = when (e) {
    E.A -> E.konstues("OK")
}

fun box(): String {
    return f(E.A)
}
