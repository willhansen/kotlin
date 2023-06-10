// FILE:a.kt
package a

import b.B        //class
import b.foo      //function
import b.ext      //extension function
import b.konstue    //property
import b.C.Companion.bar    //function from companion object
import b.C.Companion.cValue //property from companion object
import b.<!UNRESOLVED_IMPORT!>constant<!>.fff     //function from konst
import b.<!UNRESOLVED_IMPORT!>constant<!>.dValue  //property from konst
import b.constant
import b.E.Companion.f      //konst from companion object
import <!UNRESOLVED_IMPORT!>smth<!>.illegal
import b.C.<!UNRESOLVED_IMPORT!>smth<!>.illegal
import b.<!UNRESOLVED_IMPORT!>bar<!>.smth
import b.<!UNRESOLVED_IMPORT!>bar<!>.*
import b.<!UNRESOLVED_IMPORT!>unr<!>.unr.unr
import <!UNRESOLVED_IMPORT!>unr<!>.unr.unr

fun test(arg: B) {
    foo(konstue)
    arg.ext()

    bar()
    foo(cValue)

    <!UNRESOLVED_REFERENCE!>fff<!>(<!UNRESOLVED_REFERENCE!>dValue<!>)

    constant.fff(constant.dValue)

    f.f()
}

// FILE:b.kt
package b

class B() {}

fun foo(i: Int) = i

fun B.ext() {}

konst konstue = 0

class C() {
    companion object {
        fun bar() {}
        konst cValue = 1
    }
}

class D() {
    fun fff(s: String) = s
    konst dValue = "w"
}

konst constant = D()

class E() {
    companion object {
        konst f = F()
    }
}

class F() {
    fun f() {}
}

fun bar() {}

//FILE:c.kt
package c

import c.<!CANNOT_ALL_UNDER_IMPORT_FROM_SINGLETON!>C<!>.*

object C {
    fun f() {
    }
    konst i = 348
}

fun foo() {
    if (<!UNRESOLVED_REFERENCE!>i<!> == 3) <!UNRESOLVED_REFERENCE!>f<!>()
}

//FILE:d.kt
package d

import d.A.Companion.B
import d.A.Companion.C

konst b : B = B()
konst c : B = C

class A() {
    companion object {
        open class B() {}
        object C : B() {}
    }
}
