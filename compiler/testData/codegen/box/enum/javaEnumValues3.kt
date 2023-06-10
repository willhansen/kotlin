// TARGET_BACKEND: JVM
// FILE: E.java
public enum E {
    OK(), A();
    public static void konstues(boolean b) {}
}

// FILE: test.kt

fun f(e: E) = when (e) {
    E.A -> E.konstues()[0].toString()
    else -> "?"
}

fun box(): String {
    return f(E.A)
}
