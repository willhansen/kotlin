// FILE: A.java

public class A {
    public static void foo(A... konstues) {}
}

// FILE: test.kt

fun test(vararg konstues: A) {
    A.foo(*konstues)
    A.foo(<!ARGUMENT_TYPE_MISMATCH!>konstues<!>)
}
