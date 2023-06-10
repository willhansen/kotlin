// !DIAGNOSTICS: -UNUSED_VARIABLE

object X {
    interface A

    object B
    class C
}

fun testX() {
    konst interface_as_fun = X.<!RESOLUTION_TO_CLASSIFIER!>A<!>()
    konst interface_as_konst = X.<!NO_COMPANION_OBJECT!>A<!>

    konst object_as_fun = X.<!FUNCTION_EXPECTED!>B<!>()
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

    konst object_as_fun = Y.<!FUNCTION_EXPECTED!>B<!>()
    konst class_as_konst = Y.<!NO_COMPANION_OBJECT!>C<!>
}

fun test(x: X) {
    konst interface_as_fun = x.<!RESOLUTION_TO_CLASSIFIER!>A<!>()
    konst interface_as_konst = x.<!NESTED_CLASS_ACCESSED_VIA_INSTANCE_REFERENCE, NO_COMPANION_OBJECT!>A<!>

    konst object_as_fun = x.<!RESOLUTION_TO_CLASSIFIER!>B<!>()
    konst class_as_konst = x.<!NESTED_CLASS_ACCESSED_VIA_INSTANCE_REFERENCE, NO_COMPANION_OBJECT!>C<!>
}
