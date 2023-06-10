// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// FILE: javaDefaultMethod.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class K(konst k: String) : J {
    override fun get2() = k
}

fun box(): String {
    konst k = K("K")

    konst test1 = k.get1() + k.get2()
    if (test1 != "OK") throw AssertionError("test1: $test1")

    konst j: J = k
    konst test2 = j.get1() + j.get2()
    if (test2 != "OK") throw AssertionError("test2: $test2")

    konst test3 = JT.test(k)
    if (test3 != "OK") throw AssertionError("test3: $test3")

    return "OK"
}

// FILE: J.java
public interface J {
    default String get1() { return "O"; }
    default String get2() { return "Failed"; }
}

// FILE: JT.java
public class JT {
    public static String test(J j) {
        return j.get1() + j.get2();
    }
}