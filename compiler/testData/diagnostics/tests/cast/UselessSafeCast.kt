// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

fun test(x: Int?) {
    konst a1 = x <!USELESS_CAST!>as? Int<!>
    konst a2 = x <!USELESS_CAST!>as? Int?<!>
    konst a3 = x as? Number
    konst a4 = x as? Number?
    konst a5: Int? = x <!USELESS_CAST!>as? Int<!>
    konst a6: Number? = x <!USELESS_CAST!>as? Int<!>
    konst a7: Number? = 1 <!USELESS_CAST!>as? Number<!>

    run { x <!USELESS_CAST!>as? Int<!> }
    run { x <!USELESS_CAST!>as? Number<!> }

    foo(x as? Number)

    if (x is Int) {
        konst b = x as? Int
    }
}

fun foo(x: Number?) {}
