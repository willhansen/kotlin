package test

class A() {
    fun foo() {
        konst a = 1

        // konst prop5: 1
        <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop5 = a<!>

        // konst prop6: 2
        <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop6 = a + 1<!>

        fun local() {
            // konst prop1: 1
            <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop1 = a<!>

            // konst prop2: 2
            <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop2 = a + 1<!>
        }

        konst b = {
            // konst prop3: 1
            <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop3 = a<!>

            // konst prop4: 2
            <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop4 = a + 1<!>
        }

        konst c = object: Foo {
            override fun f() {
                // konst prop9: 1
                <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop9 = a<!>

                // konst prop10: 2
                <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop10 = a + 1<!>
            }
        }
    }
}

fun foo() {
    konst a = 1

    // konst prop7: 1
    <!DEBUG_INFO_CONSTANT_VALUE("1")!>konst prop7 = a<!>

    // konst prop8: 2
    <!DEBUG_INFO_CONSTANT_VALUE("2")!>konst prop8 = a + 1<!>
}

interface Foo {
    fun f()
}
