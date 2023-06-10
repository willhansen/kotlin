// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

var b = true

if (b) {
    konst x = 3
}

konst y = <!UNRESOLVED_REFERENCE!>x<!>

