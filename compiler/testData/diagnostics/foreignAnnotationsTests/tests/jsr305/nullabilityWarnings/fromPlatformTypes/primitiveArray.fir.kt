// JSR305_GLOBAL_REPORT: warn

// FILE: J.java
public class J {
    @MyNonnull
    public static int[] staticNN;
    @MyNullable
    public static int[] staticN;
    public static int[] staticJ;
}

// FILE: k.kt
fun test() {
    konst platformNN = J.staticNN
    konst platformN = J.staticN
    konst platformJ = J.staticJ

    platformNN[0]
    platformN[0]
    platformJ[0]

    platformNN[0] = 1
    platformN[0]  = 1
    platformJ[0]  = 1
}
