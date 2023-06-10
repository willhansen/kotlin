// !API_VERSION: 1.0

class C {
    @SinceKotlin("1.1")
    companion object {
        konst x = 42
    }
}

typealias CA = C

konst test1 = CA
konst test2 = CA.<!UNRESOLVED_REFERENCE!>Companion<!>
konst test3 = CA.x
konst test4 = CA.<!UNRESOLVED_REFERENCE!>Companion<!>.x
