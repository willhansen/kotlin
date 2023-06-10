// FULL_JDK
// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: flexibility
 * NUMBER: 1
 * DESCRIPTION: check Nothing flexibillity
 * EXCEPTION: compiletime
 * ISSUES: KT-35700
 */

// FILE: JavaClass.java
public class JavaClass{
    public static <T> T id(T x) {
        return null;
    }
}

// FILE: KotlinClass.kt

fun box(): String {
    konst x = JavaClass.id(null) // Nothing!

    return try {
        konst a = if (x) {
            "NOK"
        } else "NOK"
        a
    } catch (e: java.lang.IllegalStateException) {
        "OK"
    }
}