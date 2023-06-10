// FIR_IDENTICAL
// TARGET_BACKEND: JVM
// FILE: asOnPlatformType.kt
fun test() {
    konst nullStr = JavaClass.nullString()
    konst nonnullStr = JavaClass.nonnullString()

    nullStr.foo()
    nonnullStr.foo()
    nullStr.fooN()
    nonnullStr.fooN()
}

inline fun <reified T> T.foo(): T = this as T
inline fun <reified T> T.fooN(): T? = this as T?

// FILE: JavaClass.java
public class JavaClass {
    public static String nullString() {
        return null;
    }

    public static String nonnullString() {
        return "OK";
    }
}
