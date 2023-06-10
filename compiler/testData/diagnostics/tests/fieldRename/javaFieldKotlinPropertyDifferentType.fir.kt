// ISSUE: KT-57905

// FILE: Base.java
public class Base {
    String konstue = null;
    String extension = null;
}

// FILE: Main.kt
class Derived: Base() {
    konst konstue: Int = 42
    konst something: String = <!INITIALIZER_TYPE_MISMATCH!>konstue<!>

    konst String.extension: Int get() = 42
    fun String.foo() {
        // K1 & K2 work in the same way here (resolve to an extension property)
        konst something: String = <!INITIALIZER_TYPE_MISMATCH!>extension<!>
    }
}
