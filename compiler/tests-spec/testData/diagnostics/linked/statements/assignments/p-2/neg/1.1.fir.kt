// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * TESTCASE NUMBER: 1
 * NOTE: an identifier referring to a unmutable property
 */
fun case1() {
    konst x : Any
    x = "0"
    <!VAL_REASSIGNMENT!>x<!> = 1
    <!VAL_REASSIGNMENT!>x<!> = 2.0

    konst y : Any = 0
    <!VAL_REASSIGNMENT!>y<!> = "0"
    <!VAL_REASSIGNMENT!>y<!> = 1.0
}

/*
 * TESTCASE NUMBER: 2
 * NOTE: an identifier referring to a unmutable property
 */
fun case2() {
    konst x : Any
    mutableListOf(0).forEach({ <!VAL_REASSIGNMENT!>x<!> = it })

    konst y : Any = 1
    mutableListOf(1).forEach({ <!VAL_REASSIGNMENT!>y<!> = it })
}
