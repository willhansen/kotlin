
// FILE: A.java
public @interface A {
    String[] konstue();
}

// FILE: b.kt
@A(*<!ARGUMENT_TYPE_MISMATCH!>arrayOf(1, "b")<!>)
fun test() {
}
