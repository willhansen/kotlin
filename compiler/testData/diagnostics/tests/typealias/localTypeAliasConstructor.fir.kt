// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class Cell<TC>(konst x: TC)

fun <T> id(x: T): T {
    typealias C = Cell<T>
    class Local(konst cell: <!UNRESOLVED_REFERENCE!>C<!>)
    konst cx = <!UNRESOLVED_REFERENCE!>C<!>(x)
    konst c: <!UNRESOLVED_REFERENCE!>C<!> = Local(cx).cell
    return c.<!UNRESOLVED_REFERENCE!>x<!>
}
