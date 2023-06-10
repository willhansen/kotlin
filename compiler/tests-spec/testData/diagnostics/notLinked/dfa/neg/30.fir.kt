// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_VARIABLE -UNUSED_VALUE
// SKIP_TXT

// TESTCASE NUMBER: 1
class Case1(konst x: Any?) {
    konst y = x!!
    konst z: Any = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?"), INITIALIZER_TYPE_MISMATCH!>x<!>
}

// TESTCASE NUMBER: 2
class Case2(konst y: Any?): ClassWithCostructorParam(y!!) {
    konst z: Any = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?"), INITIALIZER_TYPE_MISMATCH!>y<!>
}

// TESTCASE NUMBER: 3
class Case3(konst y: Any?): ClassWithCostructorParam(y as Class) {
    konst z: Class = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any?"), INITIALIZER_TYPE_MISMATCH!>y<!>
}

// TESTCASE NUMBER: 4
class Case4(konst y: Any?): ClassWithCostructorParam(y!!) {
    init {
        konst z: Any = <!INITIALIZER_TYPE_MISMATCH!>y<!>
    }
}

// TESTCASE NUMBER: 5
class Case5(konst y: Any?): ClassWithCostructorParam(y as Interface1), Interface1 by <!TYPE_MISMATCH!>y<!> {}

// TESTCASE NUMBER: 6
fun case_6(a: Int?) = object : ClassWithCostructorParam(a!!) {
    fun run() = a<!UNSAFE_CALL!>.<!>toShort()
    init {
        println(a<!UNSAFE_CALL!>.<!>toShort())
    }
}
