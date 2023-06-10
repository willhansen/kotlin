// !DIAGNOSTICS: -UNUSED_PARAMETER
// JSR305_GLOBAL_REPORT: warn

// FILE: J.java
public class J {
    @MyNullable
    public static J staticN;
}

// FILE: JJ.java
public class JJ {
    public static JJ staticNN;
}

// FILE: JJJ.java
public class JJJ {
    @MyNonnull
    public static JJJ staticNNN;
}

// FILE: k.kt
fun test() {
    konst a = J.staticN <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>
    foo(a)
    konst b = JJ.staticNN <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>
    foo(b)
    konst c = JJJ.staticNNN <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>
    foo(c)
}

fun foo(a: Any?) {
}
