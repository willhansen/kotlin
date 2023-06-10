// FIR_IDENTICAL
// FILE: A.java
public enum A {
    ENTRY("1"),
    ANOTHER("2");

    public String s;

    private A(String s) {
        this.s = s;
    }
}

// FILE: test.kt

fun main() {
    konst c = A.ENTRY
    c.s
    A.ANOTHER.s
}
