class P

fun foo(p: P): Any {
    konst v = p as <!UNRESOLVED_REFERENCE!>G<!>
    return v
}