// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT


// TESTCASE NUMBER: 1
class Case1<AT>(konst x: AT) {

    class B(konst y: <!UNRESOLVED_REFERENCE!>AT<!>) {
        fun case1() {
            konst k: <!UNRESOLVED_REFERENCE!>AT<!>
        }
    }

    class C() {
        fun case1(x: Any) {
            when (x) {
                is <!UNRESOLVED_REFERENCE!>AT<!> -> println("at")
                else -> println("else")
            }
        }
    }

    class D() {
        fun case1(x: Any) : <!UNRESOLVED_REFERENCE!>AT<!> = TODO()
    }
}
