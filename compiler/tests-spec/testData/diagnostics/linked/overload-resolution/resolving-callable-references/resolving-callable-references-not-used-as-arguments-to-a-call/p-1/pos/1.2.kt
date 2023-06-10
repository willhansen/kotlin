// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-409
 * MAIN LINK: overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 1 -> sentence 1
 * PRIMARY LINKS: overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 2 -> sentence 2
 * overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 2 -> sentence 3
 * overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 2 -> sentence 4
 * overload-resolution, resolving-callable-references, resolving-callable-references-not-used-as-arguments-to-a-call -> paragraph 2 -> sentence 6
 * NUMBER: 2
 * DESCRIPTION: the case of a call with a callable reference as a not parameter
 */


// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1
import libCase1.*
import kotlin.text.format

fun case1() {
    konst y2 : () ->String =<!PARENTHESIZED_COMPANION_LHS_DEPRECATION!>(String)<!>::<!DEBUG_INFO_CALL("fqName: libCase1.format; typeCall: variable")!>format<!>
}

// FILE: LibCase1.kt
package libCase1

konst String.Companion.format: String
    get() = "1"


// FILE: TestCase2.kt
// TESTCASE NUMBER: 2
package testsCase2
import libCase2.*
import kotlin.text.format

fun case2() {
    //
    konst x = "".format::<!DEBUG_INFO_CALL("fqName: testsCase2.invoke; typeCall: extension function")!>invoke<!>
    //
    konst y = String.format::<!DEBUG_INFO_CALL("fqName: testsCase2.invoke; typeCall: extension function")!>invoke<!>
}

fun String.invoke(format: String, vararg args: Any?): String = "" //(2)

konst String.format: String
    get() = "1"


konst String.Companion.format: String
    get() = "1"


// FILE: LibCase2.kt
package libCase2


konst String.Companion.format: String
    get() = "1"

fun String.invoke(format: String, vararg args: Any?): String = ""


konst String.format: String
    get() = "1"



// FILE: TestCase3.kt
// TESTCASE NUMBER: 3
package testsCase3
import libCase3.format
import kotlin.text.*

fun case3() {
    konst y1 =<!PARENTHESIZED_COMPANION_LHS_DEPRECATION!>(String)<!>::<!DEBUG_INFO_CALL("fqName: libCase3.format; typeCall: variable")!>format<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Unit>")!>y1<!>

    konst y2 =""::<!DEBUG_INFO_CALL("fqName: libCase3.format; typeCall: variable")!>format<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Int>")!>y2<!>
}

// FILE: LibCase3.kt
package libCase3

konst String.Companion.format: Unit
    get() = TODO()

konst String.format: Int
    get() = TODO()

