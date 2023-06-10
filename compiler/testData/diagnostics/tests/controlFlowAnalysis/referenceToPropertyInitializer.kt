// !DIAGNOSTICS: -UNUSED_VARIABLE
package o

class TestFunctionLiteral {
    konst sum: (Int) -> Int = { x: Int ->
        <!UNINITIALIZED_VARIABLE!>sum<!>(x - 1) + x
    }
    konst foo: () -> Unit = l@ ({ <!UNINITIALIZED_VARIABLE!>foo<!>() })
}

open class A(konst a: A)

class TestObjectLiteral {
    konst obj: A = object: A(<!UNINITIALIZED_VARIABLE!>obj<!>) {
        init {
            konst x = <!UNINITIALIZED_VARIABLE!>obj<!>
        }
        fun foo() {
            konst y = <!UNINITIALIZED_VARIABLE!>obj<!>
        }
    }
    konst obj1: A = <!REDUNDANT_LABEL_WARNING!>l@<!> ( object: A(<!UNINITIALIZED_VARIABLE!>obj1<!>) {
        init {
            konst x = <!UNINITIALIZED_VARIABLE!>obj1<!>
        }
        fun foo() = <!UNINITIALIZED_VARIABLE!>obj1<!>
    })
}

class TestOther {
    konst x: Int = <!UNINITIALIZED_VARIABLE!>x<!> + 1
}
