// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT



// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1
import libCase1.*
import kotlin.text.format

fun case1() {
    konst y1 =(String)::<!OVERLOAD_RESOLUTION_AMBIGUITY!>format<!>
}

// FILE: LibCase1.kt
package libCase1

konst String.Companion.format: String
    get() = "1"


// FILE: TestCase2.kt
// TESTCASE NUMBER: 2
package testsCase2
import libCase2.*
import kotlin.text.*

fun case2() {
    konst y1 =(String)::<!OVERLOAD_RESOLUTION_AMBIGUITY!>format<!>
}

// FILE: LibCase2.kt
package libCase2

konst String.Companion.format: String
    get() = "1"
