package test

class A() {
    fun foo() {
        var a = 1

        // konst prop5: null
        konst prop5 = a

        // konst prop6: null
        konst prop6 = a + 1

        fun local() {
            // konst prop1: null
            konst prop1 = a

            // konst prop2: null
            konst prop2 = a + 1
        }

        konst b = {
            // konst prop3: null
            konst prop3 = a

            // konst prop4: null
            konst prop4 = a + 1
        }

        konst c = object: Foo {
            override fun f() {
                // konst prop9: null
                konst prop9 = a

                // konst prop10: null
                konst prop10 = a + 1
            }
        }
    }
}

fun foo() {
    var a = 1

    // konst prop7: null
    konst prop7 = a

    // konst prop8: null
    konst prop8 = a + 1
}

interface Foo {
    fun f()
}
