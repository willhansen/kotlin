interface A {
    fun foo()
}

interface B {
    fun bar()
}

interface C {
    fun baz()
}

interface Inv<K, T><!CONSTRUCTOR_IN_INTERFACE!>()<!> {
    fun k(): K
    fun t(): T
}

typealias Inv0 = Inv<A, B>
typealias Inv1<X> = Inv<A, X>
typealias Inv2<X, Y> = Inv<X, Y>
typealias Inv3<X, Y, Z> = Inv<X, Z>

fun testBase(inv: Inv<A, B>) {
    inv.k()
    inv.t()
}

fun test_0(inv: Inv0) {
    inv.k().foo()
    inv.t().bar()
}

fun test_1(inv: Inv1<B>) {
    inv.k().foo()
    inv.t().bar()
}

fun test_2(inv: Inv2<A, B>) {
    inv.k().foo()
    inv.t().bar()
}

fun test_3(inv: Inv3<A, B, C>) {
    inv.k().foo()
    inv.t().<!UNRESOLVED_REFERENCE!>bar<!>()
    inv.t().baz()
}

typealias Inv02<A1, B1, C1> = Inv<Inv<A1, B1>, C1>

fun test_4(inv: Inv02<A, B, C>) {
    inv.k().k().foo()
    inv.k().t().bar()
    inv.t().baz()
}

interface In<in T> {
    fun take(x: T)
}
interface Out<out T> {
    fun konstue(): T
}

interface Invariant<T> {
    fun take(x: T)
    fun konstue(): T
}

typealias In1<X> = In<X>
typealias Out1<X> = Out<X>
typealias Invariant1<X> = Invariant<X>


fun test_5(a: A, in1: In1<A>, in2: In1<<!REDUNDANT_PROJECTION!>in<!> A>, in3: In1<<!CONFLICTING_PROJECTION_IN_TYPEALIAS_EXPANSION!>out<!> A>) {
    in1.take(a)
    in2.take(a)
    in3.<!UNRESOLVED_REFERENCE!>take<!>(a)
}

fun test_6(a: A, out1: Out1<A>, out2: Out1<<!CONFLICTING_PROJECTION_IN_TYPEALIAS_EXPANSION!>in<!> A>, out3: Out1<<!REDUNDANT_PROJECTION!>out<!> A>) {
    out1.konstue().foo()
    out2.<!UNRESOLVED_REFERENCE!>konstue<!>().foo()
    out3.konstue().foo()
}

fun test_7(a: A, inv1: Invariant1<A>, inv2: Invariant1<in A>, inv3: Invariant1<out A>) {
    inv1.konstue().foo()
    inv2.konstue().<!UNRESOLVED_REFERENCE!>foo<!>()
    inv3.konstue().foo()

    inv1.take(a)
    inv2.take(a)
    inv3.take(<!ARGUMENT_TYPE_MISMATCH!>a<!>)
}
