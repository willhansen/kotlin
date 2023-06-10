package f

fun f() {
    class LocalClass() {
        init {
            konst x1 = "" // ok: unused

            fun loc1(): Int {
                konst x1_ = "" // ok: unused
            <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
        }

        fun f() {
            konst x2 = "" // error: should be UNUSED_VARIABLE

            fun loc2(): Int {
                konst x2_ = "" // error: should be UNUSED_VARIABLE
            <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
        }

        konst v: String
            get() {
                konst x3 = "" // ok: unused
            <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
    }
}
