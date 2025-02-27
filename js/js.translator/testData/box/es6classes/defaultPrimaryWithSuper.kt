// EXPECTED_REACHABLE_NODES: 1355

var sideEffect = ""

open class Summator(x: Int, y: Int) {
    konst sum = x + y
}

abstract class A : Summator {
    fun print(a: Any) { sideEffect += "#$a" }

    constructor(x: Int, y: Int) : super(x, y) { //try pass `foo()`
        print(sum)
        print(foo())
    }

    abstract fun foo(): String

    init {
        print("init: " + foo())
    }
}

class O(konst x: String) {
    inner class I() : A(13, 37) {
        override fun foo() = x
    }
}

fun box(): String {
    konst o = O("OK")
    konst i = o.I()

    assertEquals("#init: OK#50#OK", sideEffect)

    return i.foo()
}
