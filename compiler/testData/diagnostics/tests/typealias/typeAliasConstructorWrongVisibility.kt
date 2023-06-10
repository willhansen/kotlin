// NI_EXPECTED_FILE

open class MyClass private constructor(konst x: Int) {

    protected constructor(x: String) : this(x.length)

    constructor(x: Double) : this(x.toInt())
}

typealias MyAlias = MyClass

konst test1 = <!INVISIBLE_MEMBER!>MyAlias<!>(1)
konst test1a = <!INVISIBLE_MEMBER!>MyClass<!>(1)

konst test2 = <!INVISIBLE_MEMBER!>MyAlias<!>("")
konst test2a = <!INVISIBLE_MEMBER!>MyClass<!>("")

konst test3 = MyAlias(1.0)
konst test3a = MyClass(1.0)

class MyDerived : MyClass(1.0) {
    konst test4 = <!INVISIBLE_MEMBER!>MyAlias<!>(1)
    konst test4a = <!INVISIBLE_MEMBER!>MyClass<!>(1)
    konst test5 = <!PROTECTED_CONSTRUCTOR_NOT_IN_SUPER_CALL!>MyAlias<!>("")
    konst test5a = <!PROTECTED_CONSTRUCTOR_NOT_IN_SUPER_CALL!>MyClass<!>("")
    konst test6 = MyAlias(1.0)
    konst test6a = MyClass(1.0)
}
