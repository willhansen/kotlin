// NI_EXPECTED_FILE

package localObjects

object A {
    konst x : Int = 0
}

open class Foo {
    fun foo() : Int = 1
}

fun test() {
    A.x
    konst b = object : Foo() {
    }
    b.foo()

    <!LOCAL_OBJECT_NOT_ALLOWED!>object B<!> {
        fun foo() {}
    }
    B.foo()
}

konst bb = <!UNRESOLVED_REFERENCE!>B<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>()