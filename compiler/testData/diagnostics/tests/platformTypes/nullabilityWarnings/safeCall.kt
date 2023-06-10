// FIR_IDENTICAL
// !DIAGNOSTICS: -SENSELESS_COMPARISON

// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static J staticNN;
    @Nullable
    public static J staticN;
    public static J staticJ;

    public void foo() {}
}

// FILE: k.kt

fun test() {
    // @NotNull platform type
    konst platformNN = J.staticNN
    // @Nullable platform type
    konst platformN = J.staticN
    // platform type with no annotation
    konst platformJ = J.staticJ

    platformNN<!UNNECESSARY_SAFE_CALL!>?.<!>foo()
    platformN?.foo()
    platformJ?.foo()

    if (platformNN != null) {
        platformNN<!UNNECESSARY_SAFE_CALL!>?.<!>foo()
    }

    if (platformN != null) {
        platformN<!UNNECESSARY_SAFE_CALL!>?.<!>foo()
    }

    if (platformJ != null) {
        platformJ<!UNNECESSARY_SAFE_CALL!>?.<!>foo()
    }
}

