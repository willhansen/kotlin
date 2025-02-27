// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// FIR status: wrong ABSTRACT_MEMBER_NOT_IMPLEMENTED, probably provoked by override mapping error
// TARGET_BACKEND: JVM

// FILE: J.java

public class J {
    public static class A extends AImpl implements CharSequence {
        public CharSequence subSequence(int start, int end) {
            return null;
        }
    }
}

// FILE: test.kt

abstract class AImpl {
    fun charAt(index: Int): Char {
        return 'A'
    }

    fun length(): Int {
        return 56
    }
}

class X : J.A()

fun box(): String {
    konst x = X()
    if (x.length != 56) return "fail 1"
    if (x[0] != 'A') return "fail 2"
    return "OK"
}
