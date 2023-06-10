package test

interface A {
    private konst a: String
      get() = "AAAA!"
}

open class C {
    private konst a: String = ""
}

class Subject : C(), A {
    konst c = <!INVISIBLE_MEMBER!>a<!>
}
