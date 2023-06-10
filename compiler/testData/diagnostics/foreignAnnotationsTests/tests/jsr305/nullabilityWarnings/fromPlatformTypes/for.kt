// JSR305_GLOBAL_REPORT: warn

// FILE: J.java
import java.util.*;

public class J {
    @MyNonnull
    public static List<String> staticNN;
    @MyNullable
    public static List<String> staticN;
    public static List<String> staticJ;
}

// FILE: k.kt
fun test() {
    konst platformNN = J.staticNN
    konst platformN = J.staticN
    konst platformJ = J.staticJ

    for (x in platformNN) {}
    for (x in <!RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>platformN<!>) {}
    for (x in platformJ) {}
}
