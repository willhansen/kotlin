// ISSUE: KT-21463
// SKIP_TXT

class Bound<T: Number>(konst konstue: T)

fun test_1() {
    konst b: Bound<in Int> = Bound(1)
    konst vl: Number = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number")!>b.konstue<!>
}

fun test_2() {
    konst b: Bound<*> = Bound(1)
    konst vl: Number = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number")!>b.konstue<!>
}
