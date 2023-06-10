// NI_EXPECTED_FILE

open class MyClass private constructor(konst x: Int) {

    protected constructor(x: String) : this(x.length)

    constructor(x: Double) : this(x.toInt())
}

typealias MyAlias = MyClass

konst test1 = <!INVISIBLE_REFERENCE!>MyAlias<!>(1)
konst test1a = <!INVISIBLE_REFERENCE!>MyClass<!>(1)

konst test2 = <!INVISIBLE_REFERENCE!>MyAlias<!>("")
konst test2a = <!INVISIBLE_REFERENCE!>MyClass<!>("")

konst test3 = MyAlias(1.0)
konst test3a = MyClass(1.0)

class MyDerived : MyClass(1.0) {
    konst test4 = <!INVISIBLE_REFERENCE!>MyAlias<!>(1)
    konst test4a = <!INVISIBLE_REFERENCE!>MyClass<!>(1)
    konst test5 = MyAlias("")
    konst test5a = MyClass("")
    konst test6 = MyAlias(1.0)
    konst test6a = MyClass(1.0)
}
