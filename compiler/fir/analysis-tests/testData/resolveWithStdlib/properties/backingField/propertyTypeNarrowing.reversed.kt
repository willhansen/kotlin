class A {
    konst it: Number
        field = 4

    fun test() = it <!UNRESOLVED_REFERENCE!>+<!> 3

    konst p = 5
        get() = field
}

fun test() {
    konst c = A().it <!UNRESOLVED_REFERENCE!>+<!> 1
    konst d = test()
    konst b = A().p + 2
}
