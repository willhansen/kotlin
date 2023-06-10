// !DIAGNOSTICS: -UNUSED_PARAMETER
// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static J staticNN;
    @Nullable
    public static J staticN;
}

// FILE: k.kt

fun test() {
    konst n = J.staticN
    foo(<!TYPE_MISMATCH!>n<!>)
    J.staticNN = <!TYPE_MISMATCH!>n<!>
    if (n != null) {
        foo(<!DEBUG_INFO_SMARTCAST!>n<!>)
        J.staticNN = <!DEBUG_INFO_SMARTCAST!>n<!>
    }

    konst x: J? = null
    J.staticNN = <!TYPE_MISMATCH!>x<!>
    if (x != null) {
        J.staticNN = <!DEBUG_INFO_SMARTCAST!>x<!>
    }
}

fun foo(j: J) {}