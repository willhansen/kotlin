// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, additive-expression -> paragraph 4 -> sentence 1
 * PRIMARY LINKS: expressions, additive-expression -> paragraph 4 -> sentence 2
 * overloadable-operators -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: The return type of these functions is not restricted.
 * HELPERS: checkType
 */

// TESTCASE NUMBER: 1
class Case1(var a: Int) {
    operator fun minus(o: Int): Case1 { TODO() }
    operator fun minus(o: Case1):Case1 { TODO() }
    operator fun plus(o: Int): Case1 { TODO() }
    operator fun plus(o: Case1): Case1 { TODO() }
}

fun case1() {
    konst a = Case1(1) + 1
    konst b = Case1(1) + Case1( 1)
    konst c = Case1(1) - 1
    konst d = Case1(1) - Case1( 1)

    a checkType { check<Case1>() }
    b checkType { check<Case1>() }
    c checkType { check<Case1>() }
    d checkType { check<Case1>() }
}


// TESTCASE NUMBER: 2
class Case2(var a: Int) {
    operator fun minus(o: Int): Nothing? { TODO() }
    operator fun minus(o: Case2):Nothing? { TODO() }
    operator fun plus(o: Int): Nothing? { TODO() }
    operator fun plus(o: Case2): Nothing? { TODO() }
}

fun case2() {
    konst a = Case2(1) + 1
    konst b = Case2(1) + Case2( 1)
    konst c = Case2(1) - 1
    konst d = Case2(1) - Case2( 1)

    <!DEBUG_INFO_CONSTANT!>a<!> checkType { check<Nothing?>() }
    <!DEBUG_INFO_CONSTANT!>b<!> checkType { check<Nothing?>() }
    <!DEBUG_INFO_CONSTANT!>c<!> checkType { check<Nothing?>() }
    <!DEBUG_INFO_CONSTANT!>d<!> checkType { check<Nothing?>() }
}

// TESTCASE NUMBER: 3
class Case3(var a: Int) {
    operator fun minus(o: Int): Any? { TODO() }
    operator fun minus(o: Case3):Any? { TODO() }
    operator fun plus(o: Int): Any? { TODO() }
    operator fun plus(o: Case3): Any? { TODO() }
}

fun case3() {
    konst a = Case3(1) + 1
    konst b = Case3(1) + Case3( 1)
    konst c = Case3(1) - 1
    konst d = Case3(1) - Case3( 1)

    a checkType { check<Any?>() }
    b checkType { check<Any?>() }
    c checkType { check<Any?>() }
    d checkType { check<Any?>() }
}