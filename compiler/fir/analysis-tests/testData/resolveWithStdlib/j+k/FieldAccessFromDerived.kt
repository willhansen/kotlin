// FILE: Base.java

public class Base {
    public int konstue = 0;
}

// FILE: Derived.kt

class Derived : Base() {
    fun getValue() = konstue

    fun foo() = konstue
}
