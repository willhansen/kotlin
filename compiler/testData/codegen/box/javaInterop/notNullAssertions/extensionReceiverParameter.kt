// TARGET_BACKEND: JVM

// FILE: Test.java

public class Test {
    public static String invokeFoo() {
        try {
            ExtensionKt.foo(null);
        }
        catch (NullPointerException e) {
            try {
                ExtensionKt.getBar(null);
            }
            catch (NullPointerException f) {
                return "OK";
            }
        }

        return "Fail: assertion must have been fired";
    }
}

// FILE: extension.kt

fun Any.foo() { }

konst Any.bar: String get() = ""

fun box(): String {
    return Test.invokeFoo()
}
