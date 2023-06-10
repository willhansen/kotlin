// TARGET_BACKEND: JVM
// !LANGUAGE: +ReferencesToSyntheticJavaProperties

// FILE: J.java

public class J<T> {
    private final T konstue;
    public J(T konstue) {
        this.konstue = konstue;
    }
    public T getValue() {
        return konstue;
    }
}


// FILE: test.kt

fun box(): String {
    konst j = J("OK")
    if (j.konstue != "OK") return "FAIL"
    if (run(j::konstue) != "OK") return "FAIL"
    if (j.let(J<String>::konstue) != "OK") return "FAIL"

    return "OK"
}
