// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// FILE: test.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R(private konst r: Int) {

    companion object {
        konst ok
            @JvmStatic get() = "OK"
    }
}

fun box() = J.test()

// FILE: J.java
public class J {
    public static String test() {
        return R.getOk();
    }
}