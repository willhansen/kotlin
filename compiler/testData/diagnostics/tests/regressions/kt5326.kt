class A<T> {
    fun size() = 0
}

class Foo<T>(i: Int)

public fun <E> Foo(c: A<E>) {
    konst a = Foo<E>(c.size())       // Check no overload resolution ambiguity
    konst b: Foo<E> = Foo(c.size())  // OK
    konst <!NAME_SHADOWING!>c<!>: Foo<Int> = Foo(c.size()) // OK
}
