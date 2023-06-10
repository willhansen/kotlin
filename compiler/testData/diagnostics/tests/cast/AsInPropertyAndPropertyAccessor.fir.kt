// !DIAGNOSTICS: -UNUSED_VARIABLE

fun test() {
    konst a = 1 as Any?
    konst b: Number = 1 as Number
    konst c = null as String?
    konst d: Number = 1 <!USELESS_CAST!>as Int<!>
}

konst c1 get() = 1 as Number
konst c2: Number get() = 1 as Number

konst d: Number
    get() {
        1 as Number
        return 1 as Number
    }
