// FIR_IDENTICAL
// FILE: A.java

public class A extends B {
    public int getFoo() { return 0; }
}

// FILE: F.java

public class F extends B {
    public final int getFoo() { return 0; }
}

// FILE: main.kt

open class B {
    open konst foo: Int = 1
}

class C1 : A() {
    override konst foo: Int = 2
}

class C2 : F() {
    <!OVERRIDING_FINAL_MEMBER!>override<!> konst foo: Int = 3
}
