// TARGET_BACKEND: JVM

// FILE: J.java

public class J extends K {
    public final int konstue = 42;
}

// FILE: K.kt

open class K

fun box(): String {
    konst f = J::konstue
    konst a = J()
    return if (f.get(a) == 42) "OK" else "Fail: ${f.get(a)}"
}
