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
    konst bar = <!DEBUG_INFO_MISSING_UNRESOLVED!>test<!>()
    konst test = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM_ERROR!><!DEBUG_INFO_MISSING_UNRESOLVED!>bar<!>()<!>
}
