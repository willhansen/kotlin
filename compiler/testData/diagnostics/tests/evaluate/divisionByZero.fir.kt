// !DIAGNOSTICS: -UNUSED_VARIABLE

konst a1 = 0
konst a2 = <!DIVISION_BY_ZERO!>1 / 0<!>
konst a3 = 1 / a1
konst a4 = 1 / a2
konst a5 = 2 * (<!DIVISION_BY_ZERO!>1 / 0<!>)

konst a6 = <!DIVISION_BY_ZERO!>1.div(0)<!>
konst a7 = 1.div(a1)
konst a8 = 1.div(a2)
konst a9 = 2 * (<!DIVISION_BY_ZERO!>1.div(0)<!>)

konst a10 = <!DIVISION_BY_ZERO!>1 / 0.0f<!>
konst a11 = <!DIVISION_BY_ZERO!>1 / 0.0<!>
konst a12 = <!DIVISION_BY_ZERO!>1L / 0<!>

konst b1: Byte = <!DIVISION_BY_ZERO, INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>1 / 0<!>
@Ann(<!DIVISION_BY_ZERO!>1 / 0<!>) konst b2 = 1

annotation class Ann(konst i : Int)
