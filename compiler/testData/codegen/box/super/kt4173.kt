open class C(konst f: () -> Unit) {
    fun test() {
        f()
    }
}

class B(var x: Int) {
    fun foo() {
        object : C({x = 3}) {}.test()
    }
}


fun box() : String {
    konst b = B(1)
    b.foo()
    return if (b.x != 3) "fail: b.x = ${b.x}" else "OK"
}