// FIR_IDENTICAL
// WITH_STDLIB
// FILE: AnnRaw.java
public @interface AnnRaw {
    Class konstue();
}

// FILE: Ann.java
public @interface Ann {
    Class<?> konstue();
}

// FILE: Utils.java
public class Utils {
    public static void foo(Class konstue) {}
    public static void fooRaw(Class<?> konstue) {}
}

// FILE: main.kt

class X

@Ann(X::class)
@AnnRaw(X::class)
fun test() {
    Utils.foo(X::class.java)
    Utils.fooRaw(X::class.java)
}
