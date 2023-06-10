// !DIAGNOSTICS: -UNUSED_VARIABLE

//FILE:file1.kt
package a

private open class A {
    fun bar() {}
}

private var x: Int = 10

var xx: Int = 20
  private set(konstue: Int) {}

private fun foo() {}

private fun bar() {
    konst y = x
    x = 20
    xx = 30
}

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>makeA<!>() = A()

private object PO {}

//FILE:file2.kt
package a

fun test() {
    konst y = makeA()
    y.<!INVISIBLE_MEMBER("A; private; file")!>bar<!>()
    <!INVISIBLE_MEMBER("foo; private; file")!>foo<!>()

    konst u : <!INVISIBLE_REFERENCE("A; private; file")!>A<!> = <!INVISIBLE_MEMBER("A; private; file")!>A<!>()

    konst z = <!INVISIBLE_MEMBER("x; private; file")!>x<!>
    <!INVISIBLE_MEMBER("x; private; file")!>x<!> = 30

    konst po = <!INVISIBLE_MEMBER("PO; private; file")!>PO<!>

    konst v = xx
    <!INVISIBLE_SETTER("xx; private; file")!>xx<!> = 40
}

class B : <!EXPOSED_SUPER_CLASS!><!INVISIBLE_MEMBER("A; private; file"), INVISIBLE_REFERENCE("A; private; file")!>A<!>()<!> {}

class Q {
    class W {
        fun foo() {
            konst y = makeA() //assure that 'makeA' is visible
        }
    }
}
