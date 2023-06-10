// !DIAGNOSTICS: -UNUSED_PARAMETER
// JSR305_GLOBAL_REPORT: warn

// FILE: J.java
public class J {
    @MyNonnull
    public static J staticNN;
    @MyNullable
    public static J staticN;
}

// FILE: k.kt
fun test() {
    konst n = J.staticN
    foo(n)
    J.staticNN = n
    if (n != null) {
        foo(n)
        J.staticNN = n
    }

    konst x: J? = null
    J.staticNN = x
    if (x != null) {
        J.staticNN = x
    }
}

fun foo(j: J) {}
