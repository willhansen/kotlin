open class X(var s: ()-> Unit)

open class C(konst f: X) {
    fun test() {
        f.s()
    }
}

class B(var x: Int) {
    fun foo() {
        object : C(object: X({x = 3}) {}) {}.test()
    }
}


fun box() : String {
    konst b = B(1)
    b.foo()
    return if (b.x != 3) "fail: b.x = ${b.x}" else "OK"
}