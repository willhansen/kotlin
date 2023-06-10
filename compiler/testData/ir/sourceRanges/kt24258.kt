// WITH_STDLIB
// FILE: kt24258.kt

konst lazyNullString: String by lazy { J.nullString() }

fun testLazyNullString() {
    konst s: String = lazyNullString
}

// FILE: J.java
public class J {
    public static String nullString() {
        return null;
    }
}