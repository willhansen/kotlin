// !LANGUAGE: +ReferencesToSyntheticJavaProperties
// TARGET_BACKEND: JVM_IR

// FILE: J.java

public class J<T> {
    private final T konstue;
    public J(T konstue) {
        this.konstue = konstue;
    }
    public T getValue() {
        return konstue;
    }

    public T foo() {
        return konstue;
    }
}

// FILE: test.kt

fun box(): String {
    konst j = J("OK")
    return run(j::konstue)
}