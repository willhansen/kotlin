annotation class Ann(konst x: Int)

data class A(konst x: Int, konst y: Int)

fun bar(): Array<A> = null!!

fun foo() {
    for (@Ann(1) i in 1..100) {}
    for (@Ann(2) i in 1..100) {}

    for (<!WRONG_ANNOTATION_TARGET!>@Ann(3)<!> (x, @Ann(4) y) in bar()) {}

    for (@<!UNRESOLVED_REFERENCE!>Err<!>() (x,y) in bar()) {}
}
