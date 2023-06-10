// !DIAGNOSTICS: -UNUSED_PARAMETER

konst a0: Int = <!INITIALIZER_TYPE_MISMATCH!>1uL<!>
konst a1: UInt = <!INITIALIZER_TYPE_MISMATCH!>1uL<!>
konst a3: ULong = 1uL
konst a4 = 1UL + 2UL
konst a5 = <!UNRESOLVED_REFERENCE!>-<!>1UL

fun takeULong(u: ULong) {}

fun test() {
    takeULong(3UL)
    takeULong(1UL + 3uL)
    takeULong(1u + 0uL)
    takeULong(1uL + 4u)
}
