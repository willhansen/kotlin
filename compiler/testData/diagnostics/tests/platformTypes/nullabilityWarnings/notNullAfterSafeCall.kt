// FIR_IDENTICAL
// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    public @NotNull String nn() { return ""; }
}

// FILE: k.kt

fun test(j: J?) {
    konst s = j?.nn()
    if (s != null) {

    }
}