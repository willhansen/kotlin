// !DIAGNOSTICS: -UNUSED_PARAMETER
// JSR305_GLOBAL_REPORT: warn

// FILE: J.java
public class J {
    @MyNonnull
    public static J staticNN;
    @MyNullable
    public static J staticN;
    public static J staticJ;
}

// FILE: k.kt
fun test() {
    konst platformNN = J.staticNN
    konst platformN = J.staticN
    konst platformJ = J.staticJ

    platformNN.foo()
    platformN.foo()
    platformJ.foo()

    with(platformNN) {
        foo()
    }
    with(platformN) {
        foo()
    }
    with(platformJ) {
        foo()
    }

    platformNN.bar()
    platformN.bar()
    platformJ.bar()
}

fun J.foo() {}
fun J?.bar() {}
