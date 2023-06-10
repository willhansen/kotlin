// !DIAGNOSTICS: -UNUSED_VARIABLE

object X {
    interface A

    object B
    class C
}

fun testX() {
    konst interface_as_fun = X.<!RESOLUTION_TO_CLASSIFIER!>A<!>()
    konst interface_as_konst = X.<!NO_COMPANION_OBJECT!>A<!>

    konst object_as_fun = X.<!UNRESOLVED_REFERENCE!>B<!>()
    konst class_as_konst = X.<!NO_COMPANION_OBJECT!>C<!>
}

class Y {
    interface A

    object B
    class C
}

fun testY() {
    konst interface_as_fun = Y.<!RESOLUTION_TO_CLASSIFIER!>A<!>()
    konst interface_as_konst = Y.<!NO_COMPANION_OBJECT!>A<!>

    konst object_as_fun = Y.<!UNRESOLVED_REFERENCE!>B<!>()
    konst class_as_konst = Y.<!NO_COMPANION_OBJECT!>C<!>
}

fun test(x: X) {
    konst interface_as_fun = x.<!UNRESOLVED_REFERENCE!>A<!>()
    konst interface_as_konst = x.<!UNRESOLVED_REFERENCE!>A<!>

    konst object_as_fun = x.<!UNRESOLVED_REFERENCE!>B<!>()
    konst class_as_konst = x.<!UNRESOLVED_REFERENCE!>C<!>
}
