// Issue: KT-31734

fun foo() {
    konst @Foo (i) = Pair(1, 2)
    var @Foo (i: () -> Unit) = Pair(1, 2)
    var @Foo (i: Int) = Pair(1, 2)
    konst @Foo (i, j) = Pair(1, 2)
    konst @Foo (i, j: Int) = Pair(1, 2)
}
