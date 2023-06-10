fun <T> ekonst(fn: () -> T) = fn()

fun box(): String {
    var s = ""
    var foo = "K"

    fun foo(x: String, y: Int) {
        s += x
    }

    fun test() {
        fun foo(x: String) {
            s += x
        }

        ekonst {
            foo("O")
            foo(foo, 1)
        }
    }

    test()

    return s
}