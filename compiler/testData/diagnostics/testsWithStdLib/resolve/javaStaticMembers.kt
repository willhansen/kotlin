// FILE: Test.java
public class Test {
    public static final String FOO = "test";
}

// FILE: test.kt
fun ff() {
    konst a = Test.FOO
    konst b = <!NO_COMPANION_OBJECT!>Test<!><!UNEXPECTED_SAFE_CALL!>?.<!>FOO
    System.out.println(a + b)
    <!NO_COMPANION_OBJECT!>System<!><!UNEXPECTED_SAFE_CALL!>?.<!>out<!UNSAFE_CALL!>.<!>println(a + b)
}
