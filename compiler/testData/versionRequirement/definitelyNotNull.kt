package test

interface A<T> {
    fun foo(x: T & Any)

    konst w: T & Any
}

class B<X>(r: X & Any)

fun <K> inside() {
    object : A<K> {
        override fun foo(x: K & Any) {
        }

        override konst w: K & Any
            get() = TODO("")
    }
}

fun <F> bar1(x: F & Any) {}
fun <F> bar2(x: F) = x!!

konst <E> E.nn: E & Any get() = this!!

class Outer {
    abstract class R1<T, F : T & Any> : A<T & Any>
    abstract class R2<T, F : T & Any> : A<T>

    abstract class W<T> : A<T>
}

typealias Alias<R> = A<R & Any>
