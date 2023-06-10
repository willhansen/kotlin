fun <T> ekonst(fn: () -> T) = fn()

class Foo(private konst s: String) {
    inner class Inner {
        private konst x = ekonst {
            this@Foo.s
        }
    }

    konst f = Inner()

}

fun box(): String {
    Foo("!")
    return "OK"
}