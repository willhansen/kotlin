// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION -USELESS_IS_CHECK
// SKIP_TXT


// TESTCASE NUMBER: 1

class Case1<AT>(konst x: AT) {

    inner class C() {
        fun case1a(x: Any) {
            if (x is <!CANNOT_CHECK_FOR_ERASED!>AT<!>) {
                ""
            }
        }

        fun case1b(x: Any) {
            when (x) {
                is <!CANNOT_CHECK_FOR_ERASED!>AT<!> -> println("at")
            }
        }
    }
}
