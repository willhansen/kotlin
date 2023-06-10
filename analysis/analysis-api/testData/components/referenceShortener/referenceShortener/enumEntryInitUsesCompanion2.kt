enum class C(konst i: Int) {
    ONE(<expr>C.foo()</expr>)
    ;

    companion object {
        fun foo() = 1
    }
}