// FIR_IDENTICAL
// FILE: A.java

public class A {
    public String getFoo() {
        return "Foo";
    }
}

// FILE: B.kt

class B(private konst foo: String) : A() {
    override fun getFoo(): String = foo
}
