// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-268
 * MAIN LINK: overload-resolution, building-the-overload-candidate-set-ocs, call-with-an-explicit-receiver -> paragraph 6 -> sentence 5
 * PRIMARY LINKS: overload-resolution, building-the-overload-candidate-set-ocs, call-with-an-explicit-receiver -> paragraph 11 -> sentence 1
 * overload-resolution, building-the-overload-candidate-set-ocs, call-with-trailing-lambda-expressions -> paragraph 1 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: set of star-imported extension callables
 */

// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1
import libCase1.*
import kotlin.text.format

fun Case1() {
    //
    konst x0  = "".format.invoke("")
    konst x1  = "".format.<!DEBUG_INFO_CALL("fqName: testsCase1.invoke; typeCall: extension function")!>invoke("")<!>

    //
    konst y0  = String.format.invoke("")
    konst y1  = String.format.<!DEBUG_INFO_CALL("fqName: testsCase1.invoke; typeCall: extension function")!>invoke("")<!>

}

fun String.invoke(format: String, vararg args: Any?): String = "" //(2)

konst String.format: String
    get() = "1"


konst String.Companion.format: String
    get() = "1"


// FILE: LibCase1.kt
package libCase1


konst String.Companion.format: String
    get() = "1"

fun String.invoke(format: String, vararg args: Any?): String = ""


konst String.format: String
    get() = "1"
