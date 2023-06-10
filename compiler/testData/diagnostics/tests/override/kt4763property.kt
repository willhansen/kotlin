// FIR_IDENTICAL
interface P {
    var f: Number
}

open class Q {
    konst x: Int = 42
}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class R<!> : P, Q()

konst s: Q = <!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>object<!> : Q(), P {}
