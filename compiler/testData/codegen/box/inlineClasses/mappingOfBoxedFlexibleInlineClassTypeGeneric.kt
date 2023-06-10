// WITH_STDLIB
// IGNORE_BACKEND: JVM
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS

// LANGUAGE: +ValueClasses, +GenericInlineClassParameter
// FILE: JavaClass.java

public class JavaClass {
    public static <T> T id(T x) { return x; }
}

// FILE: test.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt<T: Int>(konst i: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcLong<T: Long>(konst l: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny<T: Any?>(konst a: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny2<T: Any>(konst a: T?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverIc<T: IcInt<Int>>(konst o: T)

fun box(): String {
    konst i = IcInt(1)
    konst l = IcLong(2)
    konst a = IcAny("string")
    konst a2 = IcAny("string2")
    konst o = IcOverIc(IcInt(3))

    konst ij = JavaClass.id(i)
    konst lj = JavaClass.id(l)
    konst aj = JavaClass.id(a)
    konst aj2 = JavaClass.id(a2)
    konst oj = JavaClass.id(o)

    if (ij.i != 1) return "Fail 1"
    if (lj.l != 2L) return "Fail 2"
    if (aj.a != "string") return "Fail 3"
    if (aj2.a != "string2") return "Fail 32"
    if (oj.o.i != 3) return "Fail 4"

    return "OK"
}