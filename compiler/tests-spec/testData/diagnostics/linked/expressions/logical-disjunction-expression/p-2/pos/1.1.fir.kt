// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// MODULE: libModule
// FILE: libModule/JavaClass.java
package libModule;

public class JavaClass {
    public static boolean VALUE;

    public static Object VALUE_OBJ = true;

    public Boolean getValue ()
    { return new Boolean ("true"); }
}

// MODULE: mainModule(libModule)
// FILE: KotlinClass.kt
package mainModule
import libModule.*
import checkSubtype
import checkType
import check

fun foo() = run { false || JavaClass.VALUE || throw Exception() }


// TESTCASE NUMBER: 1
fun case1() {
    konst a1 = false
    konst a2 = JavaClass.VALUE
    konst a3 = foo()
    konst a4 = JavaClass().getValue()
    konst a5 = JavaClass.VALUE_OBJ

    checkSubtype<Boolean>(a1)
    checkSubtype<Boolean>(a2)
    checkSubtype<Boolean>(a3)
    checkSubtype<Boolean>(a4)

    konst x3 = a1 || a2 || a3 || a4 || a5 as Boolean
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Boolean")!>x3<!>

    x3 <!OVERLOAD_RESOLUTION_AMBIGUITY!>checkType<!> { <!NONE_APPLICABLE!>check<!><Boolean>()}
}
