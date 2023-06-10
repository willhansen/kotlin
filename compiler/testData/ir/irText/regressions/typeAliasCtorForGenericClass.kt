class A<Q>(konst q: Q)

typealias B<X> = A<X>

typealias B2<T> = A<A<T>>

fun bar() {
    konst b = B(2)
    konst b2 = B2(b)
}
