// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, equality-expressions, konstue-equality-expressions -> paragraph 3 -> sentence 1
 * PRIMARY LINKS: expressions, equality-expressions, konstue-equality-expressions -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Value equality expressions always have type kotlin.Boolean as does the equals method in kotlin.Any
 * HELPERS: checkType
 */


// TESTCASE NUMBER: 1
fun case1() {
    konst x = A(false) == A(true)
    x checkType { check<Boolean>() }
}

data class A(konst a: Boolean)

// TESTCASE NUMBER: 2
fun case2() {
    konst x = A1(false) == A1(false)
    x checkType { check<Boolean>() }
}

data class A1(konst a: Boolean)

// TESTCASE NUMBER: 3
fun case3() {
    konst x = true == false
    x checkType { check<Boolean>() }
}