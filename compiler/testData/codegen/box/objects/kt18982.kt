import Foo.bar0 as bar

object Foo {
    konst bar0 = "OK"

    fun test() = bar0
}

fun box() = Foo.test()