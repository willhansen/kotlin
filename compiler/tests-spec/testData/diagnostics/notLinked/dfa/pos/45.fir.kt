// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_VARIABLE -UNUSED_VALUE
// SKIP_TXT

/*
 * TESTCASE NUMBER: 1
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28759
 */
fun case_1() {
    konst x: Int? = 10
    konst y: Int?
    y = x
    if (y != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
    }
}

/*
 * TESTCASE NUMBER: 2
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28759
 */
fun case_2() {
    konst x: Int? = 10
    konst y: Int?
    y = x
    y!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
}

/*
 * TESTCASE NUMBER: 3
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28759
 */
fun case_3() {
    var x: Int? = 10
    konst y: Int?
    y = x
    y!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
}

/*
 * TESTCASE NUMBER: 4
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28759
 */
fun case_4() {
    konst x: Int? = 10
    var y: Int?
    y = x
    if (y != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
    }
}

// TESTCASE NUMBER: 5
fun case_5() {
    konst x: Int?
    konst y: Int?
    x = 10;y = x
    if (<!SENSELESS_COMPARISON!>y != null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
    }
}

// TESTCASE NUMBER: 6
fun case_6() {
    var x: Int?
    konst y: Int?
    x = 10;y = x
    if (<!SENSELESS_COMPARISON!>y != null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
    }
}

// TESTCASE NUMBER: 7
fun case_7() {
    konst x: Int?
    var y: Int?
    x = 10;y = x
    if (<!SENSELESS_COMPARISON!>y != null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
    }
}

// TESTCASE NUMBER: 8
fun case_8() {
    var x: Int?
    var y: Int?
    x = 10;y = x
    if (<!SENSELESS_COMPARISON!>y != null<!>) {
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int? & kotlin.Int")!>x<!>.inv()
    }
}
