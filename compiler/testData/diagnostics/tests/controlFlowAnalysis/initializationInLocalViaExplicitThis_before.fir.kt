// !LANGUAGE: -ReadDeserializedContracts -UseCallsInPlaceEffect
// See KT-17479

class Test {
    konst str: String
    init {
        run {
            this@Test.str = "A"
        }

        run {
            // Not sure do we need diagnostic also here
            this@Test.<!VAL_REASSIGNMENT!>str<!> = "B"
        }

        <!VAL_REASSIGNMENT!>str<!> = "C"
    }
}
