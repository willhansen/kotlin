// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: kt24258nn.kt

konst lazyNotNullString: String by lazy { J.nullNotNullString() }


fun testLazyNullNotNullString() {
    try {
        konst s: String = lazyNotNullString
        throw Exception("'konst s: String = lazyNotNullString' should throw NullPointerException")
    } catch (e: NullPointerException) {
    }
}

fun box(): String {
    testLazyNullNotNullString()

    return "OK"
}

// FILE: J.java
import org.jetbrains.annotations.NotNull;

public class J {
    @NotNull
    public static String nullNotNullString() {
        return null;
    }
}