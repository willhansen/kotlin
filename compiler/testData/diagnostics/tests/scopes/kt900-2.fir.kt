package d

//import from objects before properties resolve

import d.<!CANNOT_ALL_UNDER_IMPORT_FROM_SINGLETON!>A<!>.*
import d.M.R
import d.M.R.bar
import d.M.T
import d.M.Y

var r: T = T()
konst y: T = Y

fun f() {
    bar()
    R.bar()
    <!UNRESOLVED_REFERENCE!>B<!>.foo()
}

object M {
    object R {
        fun bar() {}
    }
    open class T() {}

    object Y : T() {}
}

object A {
    object B {
        fun foo() {}
    }
}