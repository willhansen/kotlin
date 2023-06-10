// EXPECTED_REACHABLE_NODES: 1378
package foo

open class Base {
    konst i: Int
    konst i2: Int
    konst i3: Int
    konst bs: String

    constructor(ii1: Int, ii2: Int) {
        i = ii1
        i2 = ii2
        i3 = 30
    }

    constructor(ii: Int):  this(ii, 25) {
    }

    open fun foo() = bs

    init {
        bs = "fail"
    }
}

class Test(konst tt: String) : Base(18) {

    override fun foo() = tt
}

fun box(): String {

    konst t = Test("OK")
    return t.foo()
}