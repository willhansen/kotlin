// !LANGUAGE: -ReadDeserializedContracts -UseCallsInPlaceEffect
// See KT-17479

class Test {
    konst str: String
    init {
        run {
            <!CAPTURED_MEMBER_VAL_INITIALIZATION!>this@Test.str<!> = "A"
        }

        run {
            // Not sure do we need diagnostic also here
            this@Test.str = "B"
        }

        str = "C"
    }
}