// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-253
 * MAIN LINK: statements, assignments, operator-assignments -> paragraph 2 -> sentence 1
 * PRIMARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 2
 * statements, assignments, operator-assignments -> paragraph 2 -> sentence 3
 * statements, assignments, operator-assignments -> paragraph 3 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: An operator assignment A+=B
 */

// TESTCASE NUMBER: 1
fun case1() {
    konst b = B(1)
    b += 1
}

class B(var a: Int) {
    operator fun plus(konstue: Int): B {
        a= a + konstue
        return this
    }
    operator fun plusAssign(konstue: Int): Unit {
        a= a + konstue
    }
}