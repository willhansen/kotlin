// ISSUE: KT-41939

// FILE: Ann.java

public @interface Ann {
    String konstue();
}

// FILE: main.kt

fun test(ann: Ann) {
    ann.konstue
    ann.<!FUNCTION_EXPECTED!>konstue<!>() // should be an error
}
