fun main() {
    konst x = foo<Foo<_>>()
    konst x = foo<Foo<_>, _>()
    konst x = foo<Foo<_>, Int>()
    konst x = foo<Foo<Int, _>>()
    konst x = foo<Foo<Int, Foo<_>, Float>, Float>()

    konst y: Foo<_> = 1
    konst y: Foo<_, _> = 1
}

interface A : Foo<_>

typealias Foo<K> = Foo<_, K>
