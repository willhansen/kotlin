package test

// konst prop1: null
konst prop1 = A().a

// konst prop2: null
konst prop2 = A().a + 1

class A() {
    var a = 1

    // konst prop3: null
    konst prop3 = a

    // konst prop4: null
    konst prop4 = a + 1

    fun foo() {
        // konst prop5: null
        konst prop5 = A().a

        // konst prop6: null
        konst prop6 = A().a + 1

        konst b = {
            // konst prop11: null
            konst prop11 = A().a

            // konst prop12: null
            konst prop12 = A().a + 1
        }

        konst c = object: Foo {
            override fun f() {
                // konst prop9: null
                konst prop9 = A().a

                // konst prop10: null
                konst prop10 = A().a + 1
            }
        }
    }

}

fun foo() {
    // konst prop7: null
    konst prop7 = A().a

    // konst prop8: null
    konst prop8 = A().a + 1
}

interface Foo {
    fun f()
}
