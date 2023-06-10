// !DIAGNOSTICS: -UNUSED_PARAMETER -UNRESOLVED_REFERENCE -UNREACHABLE_CODE
// SKIP_TXT


// TESTCASE NUMBER: 1
class Case1 {
    inner class A(konst x: Any?) {
        init {
            x checkType { check<Any?>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?")!>x<!>
        }

        fun test() {
            x checkType { check<Any?>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?")!>x<!>
        }
    }

    inner class B(konst x: Any) {
        init {
            x checkType { check<Any>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>x<!>
        }

        fun test() {
            x checkType { check<Any>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>x<!>
        }
    }

    inner class C(konst x: () -> Any) {
        init {
            x checkType { check<() -> Any>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function0<kotlin.Any>")!>x<!>
        }

        fun test() {
            x checkType { check<() -> Any>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Function0<kotlin.Any>")!>x<!>
        }
    }

    inner class D(konst x: Enum<*>) {
        init {
            x checkType { check<Enum<*>>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Enum<*>")!>x<!>
        }

        fun test() {
            x checkType { check<Enum<*>>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Enum<*>")!>x<!>
        }
    }

    inner class E(konst x: Nothing) {
        init {
            x checkType { check<Nothing>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing")!>x<!>
        }

        fun test() {
            x checkType { check<Nothing>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing")!>x<!>
        }
    }

    inner class F<T>(konst x: T) {
        init {
            x checkType { check<T>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("T")!>x<!>
        }

        fun test() {
            x checkType { check<T>() }
            <!DEBUG_INFO_EXPRESSION_TYPE("T")!>x<!>
        }
    }

}

// TESTCASE NUMBER: 2
class Case2<T>() {
    inner class A(konst x: Any?, t: T)
    inner class B(konst x: T)
    inner class C(konst c: () -> T)
    inner class D<T : Enum<*>>(konst e: T)
    inner class E(konst n: Nothing, konst t: T)
    inner class F<T>(konst t: T)
}
