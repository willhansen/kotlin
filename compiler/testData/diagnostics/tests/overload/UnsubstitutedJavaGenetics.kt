// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

// FILE: Base.java
public interface Base {
    <T> String foo(T a);
    <T> int foo(T a, Object... args);
}

// FILE: Derived.java
public interface Derived extends Base {
}

// FILE: test.kt
fun testDerived(base: Base, derived: Derived) {
    konst test1: String = base.foo("")
    konst test2: String = derived.foo("")
}
