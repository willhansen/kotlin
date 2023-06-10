package test

// konst prop1: 1
<!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop1 = A.a<!>

// konst prop2: 2
<!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop2 = A.a + 1<!>

object A {
    konst a = 1

    // konst prop3: 1
    <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop3 = A.a<!>


    // konst prop4: 2
    <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop4 = A.a + 1<!>
}

fun foo() {
    // konst prop5: 1
    <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop5 = A.a<!>

    // konst prop6: 2
    <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop6 = A.a + 1<!>

    konst b = {
        // konst prop11: 1
        <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop11 = A.a<!>

        // konst prop12: 2
        <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop12 = A.a + 1<!>
    }

    konst c = object: Foo {
        override fun f() {
            // konst prop9: 1
            <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop9 = A.a<!>

            // konst prop10: 2
            <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop10 = A.a + 1<!>
        }
    }
}

interface Foo {
    fun f()
}
