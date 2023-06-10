// NI_EXPECTED_FILE

fun test() = 3

fun <T> proxy(t: T) = t

class A {
    konst test = test()
}

class B {
    konst test = proxy(test())
}

class C {
    konst bar = test()
    konst test = <!UNRESOLVED_REFERENCE!>bar<!>()
}
