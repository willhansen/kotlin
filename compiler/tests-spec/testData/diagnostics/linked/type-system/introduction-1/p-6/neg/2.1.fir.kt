// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1() {
    konst x: Int = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst x: Any = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst x: Nothing = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 4
fun case_4() {
    var x: Int = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 5
fun case_5() {
    var x: Any = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 6
fun case_6() {
    var x: Nothing = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 7
fun case_7() {
    konst x: Int
    x = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 8
fun case_8() {
    var x: Int = 10
    x = <!NULL_FOR_NONNULL_TYPE!>null<!>
}

// TESTCASE NUMBER: 9
fun case_9() {
    konst x = null
    konst y: Int = <!INITIALIZER_TYPE_MISMATCH!>x<!>
}

// TESTCASE NUMBER: 10
fun case_10(x: Int?) {
    var y = 10
    y = <!ASSIGNMENT_TYPE_MISMATCH!>x<!>
}

// TESTCASE NUMBER: 11
fun case_11(x: Int?, y: Int = x) = null
