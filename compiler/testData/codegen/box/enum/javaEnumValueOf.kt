// TARGET_BACKEND: JVM
// FILE: E.java
public enum E {
    OK();
    public static String konstueOf(E x) {
        return x.toString();
    }
}

// FILE: test.kt

// check that both 'konstueOf(String): E' and 'konstueOf(E): String' are invoked correctly
fun box() =
    E.konstueOf(E.konstueOf("OK"))
