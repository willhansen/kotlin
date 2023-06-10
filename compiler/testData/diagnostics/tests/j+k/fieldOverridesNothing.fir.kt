// !LANGUAGE: +PreferJavaFieldOverload

// FILE: B.java

public abstract class B implements A {
    public int size = 1;
}

// FILE: main.kt

interface A {
    konst size: Int
}

class C : B() {
    override konst size: Int get() = 1
}

fun foo() {
    C().size
}
