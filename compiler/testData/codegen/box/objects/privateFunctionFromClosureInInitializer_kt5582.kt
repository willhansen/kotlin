interface T

fun <T> ekonst(fn: () -> T) = fn()

object Foo {
    private fun foo(p: T) = p

    private konst v: Int = ekonst {
        konst x = foo(O)
        42
    }

    private object O : T

    konst result = v
}

fun box(): String {
    konst foo = Foo
    if (foo.result != 42) return "Fail: ${foo.result}"
    return "OK"
}
