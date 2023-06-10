// !CHECK_TYPE
// NI_EXPECTED_FILE

konst x get() = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>x<!>

class A {
    konst y get() = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>y<!>

    konst a get() = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>b<!>
    konst b get() = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>a<!>

    konst z1 get() = id(<!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>z1<!>)
    konst z2 get() = l(<!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>z2<!>)

    konst u get() = <!UNRESOLVED_REFERENCE!>field<!>
}

fun <E> id(x: E) = x
fun <E> l(x: E): List<E> = null!!
