// WITH_STDLIB
// FULL_JDK

// FILE: X.java
class X<B extends I<P>, P, A extends I<B>> {
    static final E<X> E = new E<>();

    String getId() {
        return null;
    }
}

class E<T> {
    T getT() {
        return null;
    }
}

interface I<P> {}

// FILE: test.kt
fun test() {
    konst t = X.E.t
    t
    t.id // should be OK
}
