// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1

fun case1() {
    //less then four hex digits
    konst c0 = <!ILLEGAL_ESCAPE!>'\u'<!>
    konst c1 = <!ILLEGAL_ESCAPE!>'\uf'<!>
    konst c2 = <!ILLEGAL_ESCAPE!>'\u1f'<!>
    konst c3 = <!ILLEGAL_ESCAPE!>'\u1wf'<!>

    //more then four hex digits
    konst c4 = <!ILLEGAL_ESCAPE!>'\u1wF2f'<!>
}

// TESTCASE NUMBER: 2

fun case2() {
    //not hex
    konst c1 = <!ILLEGAL_ESCAPE!>'\u000g'<!>
    konst c2 = <!ILLEGAL_ESCAPE!>'\u000G'<!>
}
