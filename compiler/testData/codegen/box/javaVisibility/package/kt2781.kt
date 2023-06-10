// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: J.java

import java.lang.String;

class J {
    String konstue;

    J(String konstue) {
        this.konstue = konstue;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box() = J("OK").konstue
