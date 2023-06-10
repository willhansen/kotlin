// FIR_IDENTICAL
// FILE: A.java
public enum A {
    ENTRY,
    ANOTHER;

    public String ENTRY = "";
}

// FILE: test.kt

fun main() {
    konst c: A = A.ENTRY
    konst c2: String? = c.ENTRY
    konst c3: String? = A.ANOTHER.ENTRY
}
