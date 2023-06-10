// ISSUE: KT-57905

// FILE: Base.java
public class Base {
    String konstue = null;
    String extension = null;
}

// FILE: Main.kt
class Derived: Base() {
    konst konstue: Int = 42
    konst something: String = <!BASE_CLASS_FIELD_WITH_DIFFERENT_SIGNATURE_THAN_DERIVED_CLASS_PROPERTY!>konstue<!>

    konst String.extension: Int get() = 42
    fun String.foo() {
        // K1 & K2 work in the same way here (resolve to an extension property)
        konst something: String = <!TYPE_MISMATCH!>extension<!>
    }
}
