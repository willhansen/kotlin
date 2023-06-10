interface Inv<T>

fun <Y: X, X : Inv<out String>> foo(x: X, y: Y) {
    konst rX = bar(x)
    rX.length

    konst rY = bar(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
    rY.<!UNRESOLVED_REFERENCE!>length<!>
}

fun <Y> bar(l: Inv<Y>): Y = TODO()
