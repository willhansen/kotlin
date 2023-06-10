// !DIAGNOSTICS: -UNCHECKED_CAST
// !LANGUAGE: +ContextReceivers

interface A
interface B : A
typealias C = B

interface Inv<T>
interface Cov<out T>

// Functions

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A)<!>
fun f1() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!>
fun f2() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, C)<!>
fun f3() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(B, C)<!>
fun f4() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(C, C)<!>
fun f5() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A, A)<!>
fun f6() {}

context(Inv<A>, Inv<B>)
fun f7() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<A>, Inv<A>)<!>
fun f8() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<T>, Inv<A>)<!>
fun <T> f9() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<A>, Cov<B>)<!>
fun f10() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<T>, Cov<A>)<!>
fun <T> f11() {}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(T, A)<!>
fun <T> f12() {}

// Classes

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A)<!>
class C1 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!>
class C2 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, C)<!>
class C3  {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, C)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, C)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(B, C)<!>
class C4 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(B, C)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(B, C)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(C, C)<!>
class C5 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(C, C)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(C, C)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A, A)<!>
class C6 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A, A)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A, A)<!>
    fun m() {}
}

context(Inv<A>, Inv<B>)
class C7 {
    context(Inv<A>, Inv<B>)
    konst p: Any get() = 42

    context(Inv<A>, Inv<B>)
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<A>, Inv<A>)<!>
class C8 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<A>, Inv<A>)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<A>, Inv<A>)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<T>, Inv<A>)<!>
class C9<T> {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<T>, Inv<A>)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<T>, Inv<A>)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<A>, Cov<B>)<!>
class C10 {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<A>, Cov<B>)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<A>, Cov<B>)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<T>, Cov<A>)<!>
class C11<T> {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<T>, Cov<A>)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<T>, Cov<A>)<!>
    fun m() {}
}

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(T, A)<!>
class C12<T> {
    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(T, A)<!>
    konst p: Any get() = 42

    <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(T, A)<!>
    fun m() {}
}

// Properties

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A)<!>
konst p1: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!>
konst p2: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, C)<!>
konst p3: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(B, C)<!>
konst p4: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(C, C)<!>
konst p5: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, A, A)<!>
konst p6: Any?
    get() { return null }

context(Inv<A>, Inv<B>)
konst p7: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<A>, Inv<A>)<!>
konst p8: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Inv<T>, Inv<A>)<!>
konst <T> p9: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<A>, Cov<B>)<!>
konst p10: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(Cov<T>, Cov<A>)<!>
konst <T> p11: Any?
    get() { return null }

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(T, A)<!>
konst <T> p12: Any?
    get() { return null }

// Function types

// Function types are processed with the same function that is used for checking contextual declarations
// So we check here only one simple case: `context(A, B)`

<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!>
fun f(g: <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!> () -> Unit, konstue: Any): <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!> () -> Unit {
    return konstue as (<!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!> () -> Unit)
}

fun test() {
    konst lf: <!SUBTYPING_BETWEEN_CONTEXT_RECEIVERS!>context(A, B)<!> () -> Unit = { }
}
