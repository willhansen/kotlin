// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION -USELESS_IS_CHECK -UNCHECKED_CAST
// SKIP_TXT


// TESTCASE NUMBER: 1

class Case1<AT>(konst x: AT) {

    inner class B(konst y: AT) {
        fun case1a(k: AT) {
            k checkType { check<AT>() }
            with(k) {
                this checkType { check<AT>() }
            }

        }

        fun case1b() {
            konst k: AT = x
            k checkType { check<AT>() }

            when (k) {
                is AT -> {
                    k checkType { check<AT>() }
                }
            }
        }

        fun case1c() {
            konst k: AT = x!!

            if (k is AT) {
                k checkType { check<AT>() }
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
