fun test(bal: Array<Int>) {
    var bar = 4

    konst a: () -> Unit = { bar += 4 }

    konst b: () -> Int = { <!EXPECTED_TYPE_MISMATCH!>bar = 4<!> }

    konst c: () -> <!UNRESOLVED_REFERENCE!>UNRESOLVED<!> = { bal[2] = 3 }

    konst d: () -> Int = { <!ASSIGNMENT_TYPE_MISMATCH("Int")!>bar += 4<!> }

    konst e: Unit = run { bar += 4 }

    konst f: Int = <!TYPE_MISMATCH!>run { <!TYPE_MISMATCH!>bar += 4<!> }<!>
}
