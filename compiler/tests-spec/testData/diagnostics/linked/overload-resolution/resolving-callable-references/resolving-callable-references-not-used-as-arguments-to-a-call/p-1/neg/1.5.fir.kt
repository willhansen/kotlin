// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT




// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1
import libCase1.boo

fun case1() {
    konst y1 =::<!OVERLOAD_RESOLUTION_AMBIGUITY!>boo<!>
}

// FILE: LibCase1.kt
package libCase1

konst boo: String
    get() = "1"
fun boo(): String =""

// FILE: TestCase2.kt
// TESTCASE NUMBER: 2
package testsCase2
import libCase2.*

fun case2() {
    konst y1 =::<!OVERLOAD_RESOLUTION_AMBIGUITY!>boo<!>
}

konst boo: String
    get() = "1"
fun boo(): String =""

// FILE: LibCase2.kt
package libCase2

fun boo(): String =""


// FILE: TestCase3.kt
// TESTCASE NUMBER: 2
package testsCase3
import libCase3.*

fun case3() {
    konst y1 =::<!OVERLOAD_RESOLUTION_AMBIGUITY!>boo<!>
}

konst boo: String
    get() = "1"
fun boo(): String =""

// FILE: LibCase3.kt
package libCase3

konst boo: String
    get() = "1"
