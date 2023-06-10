// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR

class A(x: Int)

@JvmInline
konstue class B(konst a: A) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class C<T>(konst t: T) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class D<T : Int>(konst t: T) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class E(konst d: Double) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class F(konst e: E) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class G(konst e: Int?) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class H(konst e: Any?) {
    override fun equals(other: Any?) = true
}

@JvmInline
konstue class I(konst e: E?) {
    override fun equals(other: Any?) = true
}

fun box(): String {
    if (B(A(0)) != B(A(5))) return "Fail 1"
    if (C(0) != C("a")) return "Fail 2"
    if (D(0) != D(2)) return "Fail 3"
    if (E(0.0) != E(0.5)) return "Fail 4"
    if (F(E(0.0)) != F(E(0.5))) return "Fail 5"

    if (G(0) != G(2)) return "Fail 6.1"
    if (G(0) != G(null)) return "Fail 6.2"
    if (G(null) != G(0)) return "Fail 6.3"

    if (H("aba") != H("caba")) return "Fail 7.1"
    if (H("aba") != H(null)) return "Fail 7.2"
    if (H(null) != H("caba")) return "Fail 7.3"

    if (I(E(0.0)) != I(E(0.5))) return "Fail 8.1"
    if (I(E(0.0)) != I(null)) return "Fail 8.2"
    if (I(null) != I(E(0.5))) return "Fail 8.3"

    return "OK"
}