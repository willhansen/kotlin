// FIR_IDENTICAL
typealias MyString = String

konst x: MyString = ""
konst y = x as Any

interface Base
class Derived : Base
interface Other : Base
typealias IBase = Base
typealias IOther = Other

konst ib: IBase = Derived()
konst d = ib as Derived
konst o = ib as Other
konst io = ib as IOther
konst s = d <!CAST_NEVER_SUCCEEDS!>as<!> String
konst ms = d <!CAST_NEVER_SUCCEEDS!>as<!> MyString

