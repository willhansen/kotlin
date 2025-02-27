// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR


@JvmInline
konstue class A(konst x: Int) {
    operator fun equals(other: A) = true
}

class C

@JvmInline
konstue class B1(konst x: A)

@JvmInline
konstue class B2(konst x: A?)


@JvmInline
konstue class D1(konst x: C) {
    operator fun equals(other: D1) = true
}


@JvmInline
konstue class D2(konst x: C?) {
    operator fun equals(other: D2) = true
}

@JvmInline
konstue class E1(konst x: D1)

@JvmInline
konstue class E2(konst x: D2)

@JvmInline
konstue class F<T>(konst x: T)

@JvmInline
konstue class G<T : D1>(konst x: T)

@JvmInline
konstue class H<T>(konst x: F<T>)

fun box(): String {
    if (E1(D1(C())) != E1(D1(C()))) return "Fail 1"

    if (E2(D2(C())) != E2(D2(C()))) return "Fail 2.1"
    if (E2(D2(null)) != E2(D2(C()))) return "Fail 2.2"
    if (E2(D2(C())) != E2(D2(null))) return "Fail 2.3"
    if (E2(D2(null)) != E2(D2(null))) return "Fail 2.4"

    if (B1(A(0)) != B1(A(5))) return "Fail 3"

    if (B2(A(0)) != B2(A(5))) return "Fail 4.1"
    if (B2(null) == B2(A(5))) return "Fail 4.2"
    if (B2(A(0)) == B2(null)) return "Fail 4.3"
    if (B2(null) != B2(null)) return "Fail 4.4"

    if (F(D1(C())) != F(D1(C()))) return "Fail 5"

    if (G(D1(C())) != G(D1(C()))) return "Fail 6"

    if (H(F(1)) == H(F(2))) return "Fail 7"

    return "OK"
}