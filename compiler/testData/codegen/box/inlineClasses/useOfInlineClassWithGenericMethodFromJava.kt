// TARGET_BACKEND: JVM
// WITH_STDLIB
// LANGUAGE: +ValueClasses

// FILE: a.kt

@JvmInline
konstue class IC(konst v: Int) {
    fun <T> getT(): T? = null
}

// FILE: UseIC.java

public class UseIC {
    private IC ic = null;

    public static String result() {
        return "OK";
    }
}

// FILE: test.kt

fun box(): String {
    return UseIC.result()
}
