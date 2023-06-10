// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT



// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1
import libCase1.*
import kotlin.text.format

fun case1() {
    konst y2 : () ->String =(String)::format
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
    konst x = "".format::invoke
    //
    konst y = String.format::invoke
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
    konst y1 =(String)::format
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty1<kotlin.String, kotlin.Int>")!>y1<!>

    konst y2 =""::format
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.reflect.KProperty0<kotlin.Int>")!>y2<!>
}

// FILE: LibCase3.kt
package libCase3

konst String.Companion.format: Unit
    get() = TODO()

konst String.format: Int
    get() = TODO()
