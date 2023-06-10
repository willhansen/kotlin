open class Z(konst s: Int) {
    open fun a() {}
}

class B(konst x: Int) {
    fun foo() {
        class X : Z(x) {

        }
        X()
    }
}

fun box(): String {
    B(1).foo()
    return "OK"
}
