// TARGET_BACKEND: JVM
// MODULE: m1
// FILE: E.java
public enum E {
    OK();
    public static E konstueOf(int x) {
        if (x == 0) return OK;
        return null;
    }
}

// MODULE: m2(m1)
// FILE: test.kt

fun box(): String {
    return doIt(E.konstueOf(0))
}

fun doIt(e: E) = when (e) {
    E.OK -> "OK"
}
