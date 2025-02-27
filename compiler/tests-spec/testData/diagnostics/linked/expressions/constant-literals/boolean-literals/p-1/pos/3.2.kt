// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, boolean-literals -> paragraph 1 -> sentence 3
 * NUMBER: 2
 * DESCRIPTION: Checking of subtype for Boolean konstues
 * HELPERS: checkType
 */

// TESTCASE NUMBER: 1
fun case_1() {
    checkSubtype<Boolean?>(true)
    checkSubtype<Boolean?>(false)

    checkSubtype<Any>(true)
    checkSubtype<Any>(false)
}