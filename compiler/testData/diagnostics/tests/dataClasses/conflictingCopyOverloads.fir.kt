// !DIAGNOSTICS: -UNUSED_PARAMETER

data class A(konst x: Int, konst y: String) {
    <!CONFLICTING_OVERLOADS!>fun copy(x: Int, y: String)<!> = x
    <!CONFLICTING_OVERLOADS!>fun copy(x: Int, y: String)<!> = A(x, y)
}