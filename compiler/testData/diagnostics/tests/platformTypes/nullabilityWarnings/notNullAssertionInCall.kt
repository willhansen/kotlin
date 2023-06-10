// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static J staticNN;
}

// FILE: k.kt

fun test() {
    // @NotNull platform type
    konst platformNN = J.staticNN

    foo(platformNN<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
    konst bar = Bar()
    bar(platformNN<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
}

fun foo(a: Any) {}

class Bar {
    operator fun invoke(a: Any) {}
}