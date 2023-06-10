// TARGET_BACKEND: JVM_IR

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

// FILE: A.java

public @interface A {}

// FILE: B.java

public @interface B {
    String konstue();
}

// FILE: C.java

public @interface C {
    int[] v1();
    String v2();
}

// FILE: D.java

public @interface D {
    String konstue() default "hello";
}

// FILE: b.kt

fun box(): String {
    konst a = A()
    konst b = B("OK")
    assert(b.konstue == "OK")
    konst c = C(v2 = "v2", v1 = intArrayOf(1))
    assert(c.v2 == "v2")
    // TODO(KT-47702): Looks like we have to force users either to pass default java parameters explicitly
    // or hack LazyJavaClassDescriptor/JavaPropertyDescriptor to load annotation param default konstue,
    // because it is not stored currently anywhere.
    // konst d = D()
    konst d = D("OK").konstue
    return d
}
