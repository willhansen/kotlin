// FILE: A.java
public @interface A {
    String[] konstue();
}

// FILE: b.kt
@A(*<!TYPE_MISMATCH!>arrayOf(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>, "b")<!>)
fun test() {
}
