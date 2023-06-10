// FIR_IDENTICAL
// !LANGUAGE: +IntrinsicConstEkonstuation

// FILE: CompressionType.java
public enum CompressionType {
    OK("NOT OK");

    public final String name;
    CompressionType(String name) {
        this.name = name;
    }
}

// FILE: main.kt
const konst name = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>CompressionType.OK.name<!>
