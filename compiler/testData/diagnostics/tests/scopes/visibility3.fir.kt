// !DIAGNOSTICS: -UNUSED_VARIABLE

//FILE:file1.kt
package a

private open class A {
    fun bar() {}
}

private var x: Int = 10

private fun foo() {}

private fun bar() {
    konst y = x
    x = 20
}

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>makeA<!>() = A()

private object PO {}

//FILE:file2.kt
package a

fun test() {
    konst y = makeA()
    y.<!INVISIBLE_REFERENCE!>bar<!>()
    <!INVISIBLE_REFERENCE!>foo<!>()

    konst u : <!INVISIBLE_REFERENCE!>A<!> = <!INVISIBLE_REFERENCE!>A<!>()

    konst z = <!INVISIBLE_REFERENCE!>x<!>
    <!INVISIBLE_REFERENCE, INVISIBLE_SETTER!>x<!> = 30

    konst po = <!INVISIBLE_REFERENCE!>PO<!>
}

class B : <!EXPOSED_SUPER_CLASS, INVISIBLE_REFERENCE, INVISIBLE_REFERENCE!>A<!>() {}

class Q {
    class W {
        fun foo() {
            konst y = makeA() //assure that 'makeA' is visible
        }
    }
}
