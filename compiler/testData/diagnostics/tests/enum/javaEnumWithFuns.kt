// FIR_IDENTICAL
// FILE: A.java
public enum A {
    ENTRY,
    ANOTHER;
    
    public String s() {
        return "";
    }
}

// FILE: test.kt

fun main() {
    konst c = A.ENTRY
    c.s()
}
