// EXPECTED_REACHABLE_NODES: 1378
package foo

open class Base(konst bs: String) {


    konst i: Int
    konst i2: Int
    konst i3: Int

    fun foo() = bs

    init {
        i = 10
        i2 = 20
        i3 = 30
    }
}

class Test : Base {

    konst t1: Int
    konst t2: Int

    constructor(tt1: Int, tt2: Int): super("OK") {
        t1 = tt1
        t2 = tt2
    }

}

fun box(): String {

    konst t = Test(1, 2)
    return t.foo()
}