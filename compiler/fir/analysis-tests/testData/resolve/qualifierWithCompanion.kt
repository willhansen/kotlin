package my

class A {
    companion object X {
        fun foo() {}
    }
}

konst xx = A()

fun test() {
    konst x = A
    A.foo()
    A.X.foo()

    fun A.invoke() {}

    my.<!OPERATOR_MODIFIER_REQUIRED!>xx<!>()
}
