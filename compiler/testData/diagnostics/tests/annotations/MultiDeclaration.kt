annotation class Ann

data class Pair(konst x: Int, konst y: Int)

fun foo(): Int {
    <!WRONG_ANNOTATION_TARGET!>@Ann<!> konst (a, b) = Pair(12, 34)
    @<!UNRESOLVED_REFERENCE!>Err<!> konst (c, d) = Pair(56, 78)
    return a + b + c + d
}