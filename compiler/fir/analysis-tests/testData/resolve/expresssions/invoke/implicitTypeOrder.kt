
class A {
    fun bar() = <!OPERATOR_MODIFIER_REQUIRED!>foo<!>() // should resolve to invoke

    fun invoke() = this
}

fun create() = A()

konst foo = create()
