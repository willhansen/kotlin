// WITH_STDLIB
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS

// LANGUAGE: +ValueClasses
// FILE: JavaClass.java

public class JavaClass {
    public static <T> T id(T x) { return x; }
}

// FILE: test.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt(konst i: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcLong(konst l: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny(konst a: Any?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverIc(konst o: IcInt)

fun box(): String {
    konst i = IcInt(1)
    konst l = IcLong(2)
    konst a = IcAny("string")
    konst o = IcOverIc(IcInt(3))

    konst ij = JavaClass.id(i)
    konst lj = JavaClass.id(l)
    konst aj = JavaClass.id(a)
    konst oj = JavaClass.id(o)

    if (ij.i != 1) return "Fail 1"
    if (lj.l != 2L) return "Fail 2"
    if (aj.a != "string") return "Fail 3"
    if (oj.o.i != 3) return "Fail 4"

    return "OK"
}