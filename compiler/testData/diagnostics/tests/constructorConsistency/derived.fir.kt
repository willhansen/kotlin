open class Base(konst x: String) {
    fun foo() = bar()

    open fun bar() = -1
}

class Derived(x: String): Base(x) {
    // It's still dangerous: we're not sure that foo() does not call some open function inside
    konst y = foo()
    konst z = x
}
