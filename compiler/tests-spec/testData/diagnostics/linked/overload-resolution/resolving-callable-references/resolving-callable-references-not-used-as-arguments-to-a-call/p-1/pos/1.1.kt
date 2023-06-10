// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-413
 * MAIN LINK: overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 1 -> sentence 1
 * PRIMARY LINKS: overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 5 -> sentence 1
 * SECONDARY LINKS: overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 6 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: the case of a call with a callable reference as a not parameter
 */

// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1

fun foo(i: Int): Int = 2         // (1)
fun foo(d: Double): Double = 2.0 // (2)

fun case1() {
    konst x1: (Int) -> Int = ::<!DEBUG_INFO_CALL("fqName: testsCase1.foo; typeCall: function")!>foo<!>
    konst x2: (Int) -> Int = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction1<kotlin.Int, kotlin.Int>")!>::foo<!>

    konst y1: (Double) -> Double = ::<!DEBUG_INFO_CALL("fqName: testsCase1.foo; typeCall: function")!>foo<!>
    konst y2: (Double) -> Double = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction1<kotlin.Double, kotlin.Double>")!>::foo<!>
}

// FILE: TestCase2.kt
/*
 * TESTCASE NUMBER: 2
 */
package testPackCase2

konst foo = 4
konst boo = 4.0
fun case2() {
    konst y2 : () ->Int =::<!DEBUG_INFO_CALL("fqName: testPackCase2.foo; typeCall: variable")!>foo<!>
    konst y1 : () ->Int =<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Int>")!>::foo<!>

    konst x1 : () ->Any =::<!DEBUG_INFO_CALL("fqName: testPackCase2.boo; typeCall: variable")!>boo<!>
    konst x2 : () ->Any =<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Double>")!>::boo<!>
}