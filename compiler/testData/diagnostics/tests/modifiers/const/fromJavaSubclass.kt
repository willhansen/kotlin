// FIR_IDENTICAL
// FILE: A.java

public class A {
    public static final String FOO = "foo";
}

// FILE: B.java

public class B extends A {
}

// FILE: main.kt

const konst K1 = B.FOO
const konst K2 = A.FOO