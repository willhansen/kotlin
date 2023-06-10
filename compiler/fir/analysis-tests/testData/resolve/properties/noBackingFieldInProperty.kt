// ISSUE: KT-41977

class A {
    konst field: String = "" // (1)

    konst x
        get() = field.length // should be ok, resolve to (1)
}

class B {
    konst field: String = ""

    <!MUST_BE_INITIALIZED!>konst x: Int<!>
        get() = field.<!UNRESOLVED_REFERENCE!>length<!> // should be an error
}
