// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT


// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1

fun foo(i: Int): Int = 2         // (1)
fun foo(d: Double): Double = 2.0 // (2)

fun case1() {
    konst x1: (Int) -> Int = ::foo
    konst x2: (Int) -> Int = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KFunction1<kotlin.Int, kotlin.Int>")!>::foo<!>

    konst y1: (Double) -> Double = ::foo
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
    konst y2 : () ->Int =::foo
    konst y1 : () ->Int =<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Int>")!>::foo<!>

    konst x1 : () ->Any =::boo
    konst x2 : () ->Any =<!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Double>")!>::boo<!>
}
