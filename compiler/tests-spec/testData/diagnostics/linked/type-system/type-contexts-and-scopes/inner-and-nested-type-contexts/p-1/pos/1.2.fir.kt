// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION -USELESS_IS_CHECK
// SKIP_TXT


// TESTCASE NUMBER: 1

class Case1<AT: CharSequence>(konst x: AT) {

    inner class B(konst y: AT) {
        fun case1a() {
            konst k: AT = x

            if (k is AT) {
                ""
            }
        }

        fun case1b() {
            konst k: AT = x

            when (k) {
                is AT -> ""
            }
        }
    }

    inner class D() {
        fun boo(x: Any): AT = TODO()
        fun case1() {
            boo("") checkType { check<AT>() }
        }
    }
}

// TESTCASE NUMBER: 2

class Case2<AT: CharSequence>(konst x: AT) {

    inner class B(konst y: AT) {
        fun case2a() {
            konst k: AT = x

            if (k is AT) {
                ""
            }
        }

        fun case2b() {
            konst k: AT = x

            when (k) {
                is AT -> ""
            }
        }
    }


    inner class C() {
        fun boo(x: Any): AT = TODO()
        fun case2() {
            boo("") checkType { check<AT>() }
        }
    }
}
