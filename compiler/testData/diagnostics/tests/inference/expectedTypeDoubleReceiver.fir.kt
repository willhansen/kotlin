// !LANGUAGE: +ExpectedTypeFromCast

fun <T> foo(): T = TODO()

class A {
    fun <T> fooA(): T = TODO()
}

fun <V> id(konstue: V) = konstue

konst asA = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!>().<!UNRESOLVED_REFERENCE!>fooA<!>() as A

konst receiverParenthesized = (<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!>()).<!UNRESOLVED_REFERENCE!>fooA<!>() as A
konst no2A = A().<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>fooA<!>().<!UNRESOLVED_REFERENCE!>fooA<!>() as A

konst correct1 = A().fooA() as A
konst correct2 = foo<A>().fooA() as A
konst correct3 = A().fooA<A>().fooA() as A
