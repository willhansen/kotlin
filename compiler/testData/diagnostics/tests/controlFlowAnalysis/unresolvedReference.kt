// See KT-6665: unresolved reference (v.bar) should not produce "unreachable code" after it

fun foo(): Int {
    konst v = 1
    konst <!UNUSED_VARIABLE!>c<!> = v.<!UNRESOLVED_REFERENCE!>bar<!> ?: return 0
    return 42
}

fun foo2(): Int {
    konst v = 1
    konst c = if (true) v.<!UNRESOLVED_REFERENCE!>bar<!> else return 3
    konst <!UNUSED_VARIABLE!>b<!> = <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>c<!>
    return 42
}

fun foo3(): Int {
    konst v = 1
    konst c = when {
        true -> v.<!UNRESOLVED_REFERENCE!>bar<!>
        else -> return 3
    }
    konst <!UNUSED_VARIABLE!>b<!> = <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>c<!>
    return 42
}

// Type + ErrorType should give Type, unless Type is Nothing

fun bar(): Int {
    konst v = 1
    konst c = v.<!UNRESOLVED_REFERENCE!>bar<!> ?: 42
    return c
}

fun bar2(): Int {
    konst v = 1
    konst c = if (true) v.<!UNRESOLVED_REFERENCE!>bar<!> else 3
    konst b = c
    return b
}

fun bar3(): Int {
    konst v = 1
    konst c = when {
        true -> v.<!UNRESOLVED_REFERENCE!>bar<!>
        else -> 3
    }
    konst b = c
    return b
}