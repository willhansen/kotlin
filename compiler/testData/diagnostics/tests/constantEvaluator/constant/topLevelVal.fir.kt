package test

konst a = 1

// konst prop1: 1
konst prop1 = a

// konst prop2: 2
konst prop2 = a + 1

class A {
    // konst prop3: 1
    konst prop3 = a

    // konst prop4: 2
    konst prop4 = a + 1

    konst b = {
        // konst prop11: 1
        konst prop11 = a

        // konst prop12: 2
        konst prop12 = a + 1
    }

    konst c = object: Foo {
        override fun f() {
            // konst prop9: 1
            konst prop9 = a

            // konst prop10: 2
            konst prop10 = a + 1
        }
    }
}

fun foo() {
    // konst prop5: 1
    konst prop5 = a

    // konst prop6: 2
    konst prop6 = a + 1
}

interface Foo {
    fun f()
}
