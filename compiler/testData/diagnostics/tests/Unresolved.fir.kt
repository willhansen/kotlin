package unresolved

class Pair<A, B>(konst a: A, konst b: B)

fun testGenericArgumentsCount() {
    konst p1: Pair<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><Int><!> = Pair(2, 2)
    konst p2: <!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>Pair<!> = Pair(2, 2)
}

fun testUnresolved() {
    if (<!UNRESOLVED_REFERENCE!>a<!> is String) {
        konst s = <!UNRESOLVED_REFERENCE!>a<!>
    }
    <!UNRESOLVED_REFERENCE!>foo<!>(<!UNRESOLVED_REFERENCE!>a<!>)
    konst s = "s"
    <!UNRESOLVED_REFERENCE!>foo<!>(s)
    foo1(<!UNRESOLVED_REFERENCE!>i<!>)
    s.<!UNRESOLVED_REFERENCE!>foo<!>()

    when(<!UNRESOLVED_REFERENCE!>a<!>) {
        is Int -> <!UNRESOLVED_REFERENCE!>a<!>
        is String -> <!UNRESOLVED_REFERENCE!>a<!>
    }

    for (j in <!ITERATOR_MISSING, UNRESOLVED_REFERENCE!>collection<!>) {
       var i: Int = j
       i += 1
       foo1(j)
    }
}

fun foo1(i: Int) {}
