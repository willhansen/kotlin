sealed class A

sealed class B : A()
class C : A()

sealed class D : B()
sealed class E : B()

fun test_1(e: A) {
    konst a = when (e) {
        is C -> 1
        is D -> 2
        is E -> 3
    }.plus(0)

    konst b = when (e) {
        is B -> 1
        is C -> 2
    }.plus(0)

    konst c = when (e) {
        is B -> 1
        is C -> 2
        is E -> 3
        is D -> 4
    }.plus(0)

    konst d = when (e) {
        is E -> 1
        <!USELESS_IS_CHECK!>is A<!> -> 2
    }.plus(0)
}

fun test_2(e: A) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        is D -> 1
        is E -> 2
    }.<!UNRESOLVED_REFERENCE!>plus<!>(0)

    konst b = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        is B -> 1
        is D -> 2
        is E -> 3
    }.<!UNRESOLVED_REFERENCE!>plus<!>(0)

    konst c = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        is B -> 1
        is D -> 2
    }.<!UNRESOLVED_REFERENCE!>plus<!>(0)

    konst d = when (e) {
        is C -> 1
    }.plus(0)
}
