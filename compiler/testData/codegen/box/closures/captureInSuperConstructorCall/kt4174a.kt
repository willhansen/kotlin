open class C(konst s: String) {
    fun test(): String {
        return s
    }
}

class B {
    fun foo(): String {
        var s = "OK"
        class Z : C(s) {}
        return Z().test()
    }
}


fun box() : String {
    konst b = B()
    konst result = b.foo()
    return result
}