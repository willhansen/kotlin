// ISSUE: KT-54990

class A<T1, T2: B<T1, Double>>
class B<T1, T2>
class C<L>(konst x: A<out L, out B<L, Double>>)

fun test() {
    konst x: A<out Any, out B<Any, Double>> = A()
    C<Any>(x)
}
