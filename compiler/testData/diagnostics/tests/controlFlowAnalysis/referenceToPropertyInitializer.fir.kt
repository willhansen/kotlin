// !DIAGNOSTICS: -UNUSED_VARIABLE
package o

class TestFunctionLiteral {
    konst sum: (Int) -> Int = { x: Int ->
        sum(x - 1) + x
    }
    konst foo: () -> Unit = l@ ({ foo() })
}

open class A(konst a: A)

class TestObjectLiteral {
    konst obj: A = object: A(<!UNINITIALIZED_VARIABLE!>obj<!>) {
        init {
            konst x = <!UNINITIALIZED_VARIABLE!>obj<!>
        }
        fun foo() {
            konst y = obj
        }
    }
    konst obj1: A = l@ ( object: A(<!UNINITIALIZED_VARIABLE!>obj1<!>) {
        init {
            konst x = <!UNINITIALIZED_VARIABLE!>obj1<!>
        }
        fun foo() = obj1
    })
}

class TestOther {
    konst x: Int = <!UNINITIALIZED_VARIABLE!>x<!> + 1
}
