// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    public interface Multi {
        String component1();
        String component2();
    }

    @NotNull
    public static Multi staticNN;
    @Nullable
    public static Multi staticN;
    public static Multi staticJ;
}

// FILE: k.kt

fun test() {
    // @NotNull platform type
    konst platformNN = J.staticNN
    // @Nullable platform type
    konst platformN = J.staticN
    // platform type with no annotation
    konst platformJ = J.staticJ

    konst (a1, b1) = platformNN
    konst (a2, b2) = <!COMPONENT_FUNCTION_ON_NULLABLE, COMPONENT_FUNCTION_ON_NULLABLE!>platformN<!>
    konst (a3, b3) = platformJ
}