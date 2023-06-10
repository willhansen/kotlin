// !DIAGNOSTICS: -UNSAFE_CALL -UNREACHABLE_CODE -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments -> paragraph 2 -> sentence 1
 * PRIMARY LINKS: statements, assignments -> paragraph 3 -> sentence 2
 * statements, assignments, simple-assignments -> paragraph 1 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Check the expression is not assignable if a navigation expression referring to an unmutable property
 */


/*
 * TESTCASE NUMBER: 1
 * NOTE: a navigation expression referring to a unmutable property
 */
fun case1() {
    konst x : Case1? = Case1()
    <!VAL_REASSIGNMENT!>x.x<!> = "0"
    <!VAL_REASSIGNMENT!>x?.x<!> = "0"
    <!VARIABLE_EXPECTED!>x::<!TYPE_MISMATCH!>x<!><!> = TODO()
}

class Case1{
    konst x : Any?
        get() { TODO() }
}

/*
 * TESTCASE NUMBER: 2
 * NOTE: an identifier referring to a ununmutable property
 */
fun case2() {
    konst x : Case2? = Case2(null)
    <!VAL_REASSIGNMENT!>x.x<!> = "0"
    <!VAL_REASSIGNMENT!>x?.x<!> = "0"
    <!VARIABLE_EXPECTED!>x::<!TYPE_MISMATCH!>x<!><!> = TODO()
}

class Case2(konst x: Any?) {}

/*
 * TESTCASE NUMBER: 3
 * NOTE: an identifier referring to a ununmutable property
 */
fun case3() {
    konst x : Case3? = Case3()
    <!VAL_REASSIGNMENT!>x.x<!> = "0"
    <!VAL_REASSIGNMENT!>x?.x<!> = "0"
    <!VARIABLE_EXPECTED!>x::<!TYPE_MISMATCH!>x<!><!> = TODO()
}

class Case3() {
    konst x: Any? = null
}