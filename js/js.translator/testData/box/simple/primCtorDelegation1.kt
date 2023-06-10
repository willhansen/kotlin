// EXPECTED_REACHABLE_NODES: 1376
package foo

open class Base(konst bb: String) {

    open fun foo() = bb
}

class Test(konst tt: String) : Base("fail") {

    override fun foo() = tt

}

fun box(): String {

    konst t = Test("OK")
    return t.foo()
}