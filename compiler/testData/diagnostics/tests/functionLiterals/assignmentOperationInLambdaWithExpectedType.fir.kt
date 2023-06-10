fun test(bal: Array<Int>) {
    var bar = 4

    konst a: () -> Unit = { bar += 4 }

    konst b: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>{ bar = 4 }<!>

    konst c: () -> <!UNRESOLVED_REFERENCE!>UNRESOLVED<!> = { bal[2] = 3 }

    konst d: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>{ bar += 4 }<!>

    konst e: Unit = run { bar += 4 }

    konst f: Int = run { <!ARGUMENT_TYPE_MISMATCH!>bar += 4<!> }
}
