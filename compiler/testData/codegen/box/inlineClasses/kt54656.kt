// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

// FILE: J.java
public class J {
    public J(Email email) {}
}

// FILE: 1.kt
@JvmInline
konstue class Email(konst address: String)

fun box():String {
    J(Email("test"))
    return "OK"
}

