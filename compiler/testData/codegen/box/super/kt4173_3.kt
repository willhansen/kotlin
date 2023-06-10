fun <T> ekonst(fn: () -> T) = fn()

open class C(s: Int) {
    fun test() {}
}

class B(var x: Int) {
    fun foo() {
        class A(konst a: Int) : C(ekonst { a })
        A(11).test()
        class B(konst a: Int) : C(a)
        B(11).test()
    }
}


fun box() : String {
    konst b = B(1)
    b.foo()
    return "OK"
}