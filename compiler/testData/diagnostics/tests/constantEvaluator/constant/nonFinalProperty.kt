package test

// konst prop1: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop1 = A().a<!>

// konst prop2: null
<!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop2 = A().a + 1<!>

class A() {
    var a = 1

    // konst prop3: null
    <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop3 = a<!>

    // konst prop4: null
    <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop4 = a + 1<!>

    fun foo() {
        // konst prop5: null
        <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop5 = A().a<!>

        // konst prop6: null
        <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop6 = A().a + 1<!>

        konst b = {
            // konst prop11: null
            <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop11 = A().a<!>

            // konst prop12: null
            <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop12 = A().a + 1<!>
        }

        konst c = object: Foo {
            override fun f() {
                // konst prop9: null
                <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop9 = A().a<!>

                // konst prop10: null
                <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop10 = A().a + 1<!>
            }
        }
    }

}

fun foo() {
    // konst prop7: null
    <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop7 = A().a<!>

    // konst prop8: null
    <!DEBUG_INFO_CONSTANT_VALUE("null")!>konst prop8 = A().a + 1<!>
}

interface Foo {
    fun f()
}
