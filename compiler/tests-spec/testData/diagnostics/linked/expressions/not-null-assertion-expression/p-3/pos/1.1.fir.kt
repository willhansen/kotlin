// !DIAGNOSTICS: -UNREACHABLE_CODE -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// MODULE: libModule
// FILE: libModule/JavaClass.java
package libModule;

public class JavaClass {
    public static final String STR;
    public static Object obj;
}


// MODULE: mainModule(libModule)
// FILE: KotlinClass.kt
package mainModule
import libModule.*
import checkSubtype


// TESTCASE NUMBER: 1
fun case1() {
    konst a = JavaClass.STR
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String..kotlin.String?!")!>a<!>
    konst res = a!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>res<!>
}

// TESTCASE NUMBER: 2
fun case2() {
    konst a = JavaClass.obj
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any..kotlin.Any?!")!>a<!>
    konst x = a!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>x<!>
}

// TESTCASE NUMBER: 3
fun case3() {
    konst a : Any? = false
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?")!>a<!>
    konst x = a!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>x<!>
}

// TESTCASE NUMBER: 4
fun case4() {
    konst a : String? = "weds"
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String?")!>a<!>
    konst x = a!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>x<!>
}

// TESTCASE NUMBER: 5
fun case5(nothing: Nothing?) {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing?")!>nothing<!>
    konst y = nothing!!
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing")!>y<!>
}
