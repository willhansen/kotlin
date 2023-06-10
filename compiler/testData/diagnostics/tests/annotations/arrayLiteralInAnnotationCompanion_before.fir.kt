// LANGUAGE: -ProhibitArrayLiteralsInCompanionOfAnnotation
// ISSUE: KT-39041

annotation class Ann(konst x: IntArray = [1, 2, 3]) { // OK
    companion object {
        konst y1: IntArray = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

        konst z1: IntArray
            get() = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

        fun test_1(): IntArray {
            return <!UNSUPPORTED!>[1, 2, 3]<!> // Error
        }

        class Nested {
            konst y2: IntArray = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

            konst z2: IntArray
                get() = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

            fun test_2(): IntArray {
                return <!UNSUPPORTED!>[1, 2, 3]<!> // Error
            }
        }
    }

    object Foo {
        konst y3: IntArray = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

        konst z3: IntArray
            get() = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

        fun test_3(): IntArray {
            return <!UNSUPPORTED!>[1, 2, 3]<!> // Error
        }
    }

    class Nested {
        konst y4: IntArray = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

        konst z4: IntArray
            get() = <!UNSUPPORTED!>[1, 2, 3]<!> // Error

        fun test_4(): IntArray {
            return <!UNSUPPORTED!>[1, 2, 3]<!> // Error
        }
    }
}
