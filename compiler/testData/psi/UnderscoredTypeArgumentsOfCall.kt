fun main() {
    konst x = foo<Int, _>()
    konst x = foo<_, _, _>()
    konst x = foo<_, _, Int>()
    konst x = foo<_>()
}
