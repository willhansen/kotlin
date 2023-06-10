// !DIAGNOSTICS: -UNCHECKED_CAST
// !LANGUAGE: +ContextReceivers

interface A
interface B : A
typealias C = B

interface Inv<T>
interface Cov<out T>

// Functions

context(A, A)
fun f1() {}

context(A, B)
fun f2() {}

context(A, C)
fun f3() {}

context(B, C)
fun f4() {}

context(C, C)
fun f5() {}

context(A, A, A)
fun f6() {}

context(Inv<A>, Inv<B>)
fun f7() {}

context(Inv<A>, Inv<A>)
fun f8() {}

context(Inv<T>, Inv<A>)
fun <T> f9() {}

context(Cov<A>, Cov<B>)
fun f10() {}

context(Cov<T>, Cov<A>)
fun <T> f11() {}

context(T, A)
fun <T> f12() {}

// Classes

context(A, A)
class C1 {
    context(A, A)
    konst p: Any get() = 42

    context(A, A)
    fun m() {}
}

context(A, B)
class C2 {
    context(A, B)
    konst p: Any get() = 42

    context(A, B)
    fun m() {}
}

context(A, C)
class C3  {
    context(A, C)
    konst p: Any get() = 42

    context(A, C)
    fun m() {}
}

context(B, C)
class C4 {
    context(B, C)
    konst p: Any get() = 42

    context(B, C)
    fun m() {}
}

context(C, C)
class C5 {
    context(C, C)
    konst p: Any get() = 42

    context(C, C)
    fun m() {}
}

context(A, A, A)
class C6 {
    context(A, A, A)
    konst p: Any get() = 42

    context(A, A, A)
    fun m() {}
}

context(Inv<A>, Inv<B>)
class C7 {
    context(Inv<A>, Inv<B>)
    konst p: Any get() = 42

    context(Inv<A>, Inv<B>)
    fun m() {}
}

context(Inv<A>, Inv<A>)
class C8 {
    context(Inv<A>, Inv<A>)
    konst p: Any get() = 42

    context(Inv<A>, Inv<A>)
    fun m() {}
}

context(Inv<T>, Inv<A>)
class C9<T> {
    context(Inv<T>, Inv<A>)
    konst p: Any get() = 42

    context(Inv<T>, Inv<A>)
    fun m() {}
}

context(Cov<A>, Cov<B>)
class C10 {
    context(Cov<A>, Cov<B>)
    konst p: Any get() = 42

    context(Cov<A>, Cov<B>)
    fun m() {}
}

context(Cov<T>, Cov<A>)
class C11<T> {
    context(Cov<T>, Cov<A>)
    konst p: Any get() = 42

    context(Cov<T>, Cov<A>)
    fun m() {}
}

context(T, A)
class C12<T> {
    context(T, A)
    konst p: Any get() = 42

    context(T, A)
    fun m() {}
}

// Properties

context(A, A)
konst p1: Any?
    get() { return null }

context(A, B)
konst p2: Any?
    get() { return null }

context(A, C)
konst p3: Any?
    get() { return null }

context(B, C)
konst p4: Any?
    get() { return null }

context(C, C)
konst p5: Any?
    get() { return null }

context(A, A, A)
konst p6: Any?
    get() { return null }

context(Inv<A>, Inv<B>)
konst p7: Any?
    get() { return null }

context(Inv<A>, Inv<A>)
konst p8: Any?
    get() { return null }

context(Inv<T>, Inv<A>)
konst <T> p9: Any?
    get() { return null }

context(Cov<A>, Cov<B>)
konst p10: Any?
    get() { return null }

context(Cov<T>, Cov<A>)
konst <T> p11: Any?
    get() { return null }

context(T, A)
konst <T> p12: Any?
    get() { return null }

// Function types

// Function types are processed with the same function that is used for checking contextual declarations
// So we check here only one simple case: `context(A, B)`

context(A, B)
fun f(g: context(A, B) () -> Unit, konstue: Any): context(A, B) () -> Unit {
    return konstue as (context(A, B) () -> Unit)
}

fun test() {
    konst lf: context(A, B) () -> Unit = { }
}
