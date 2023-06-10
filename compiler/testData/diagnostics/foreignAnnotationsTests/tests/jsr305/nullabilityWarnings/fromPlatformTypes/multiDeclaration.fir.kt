// !DIAGNOSTICS: -UNUSED_VARIABLE
// JSR305_GLOBAL_REPORT: warn

// FILE: J.java
public class J {
    public interface Multi {
        String component1();
        String component2();
    }

    @MyNonnull
    public static Multi staticNN;
    @MyNullable
    public static Multi staticN;
    public static Multi staticJ;
}

// FILE: k.kt
fun test() {
    konst platformNN = J.staticNN
    konst platformN = J.staticN
    konst platformJ = J.staticJ

    konst (a1, b1) = platformNN
    konst (a2, b2) = platformN
    konst (a3, b3) = platformJ
}
