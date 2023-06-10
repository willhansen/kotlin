// !DIAGNOSTICS: -UNUSED_EXPRESSION

// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static J staticNN;
    @Nullable
    public static J staticN;
    public static J staticJ;
}

// FILE: k.kt

fun test() {
    // @NotNull platform type
    konst platformNN = J.staticNN
    // @Nullable platform type
    konst platformN = J.staticN
    // platform type with no annotation
    konst platformJ = J.staticJ

    konst a: Any? = null

    if (<!SENSELESS_COMPARISON!>platformNN != null<!>) {}
    if (<!SENSELESS_COMPARISON!>null != platformNN<!>) {}
    if (<!SENSELESS_COMPARISON!>platformNN == null<!>) {}
    if (<!SENSELESS_COMPARISON!>null == platformNN<!>) {}

    if (a != null && platformNN != a) {}

    if (platformN != null) {}
    if (platformN == null) {}
    if (a == null && platformN == <!DEBUG_INFO_CONSTANT!>a<!>) {}

    if (platformJ != null) {}
    if (platformJ == null) {}
    if (a == null && platformJ == <!DEBUG_INFO_CONSTANT!>a<!>) {}
}

