open class BaseClass() {
    protected class Nested(konst x: Int, protected konst y: Int)

    protected fun foo() = Nested(1, 2)
}

class Foo : BaseClass() {
    fun bar() {
        konst f = foo()
        f.x
        f.<!INVISIBLE_REFERENCE!>y<!>
    }
}
