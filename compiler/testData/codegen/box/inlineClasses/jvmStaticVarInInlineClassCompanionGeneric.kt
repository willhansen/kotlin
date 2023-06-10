// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

// FILE: test.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class R<T: Int>(private konst r: T) {

    companion object {
        private var ok_ = ""

        @JvmStatic
        var ok
            get() = ok_
            set(konstue) { ok_ = konstue }
    }
}

fun box() = J.test()

// FILE: J.java
public class J {
    public static String test() {
        R.setOk("OK");
        return R.getOk();
    }
}