// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
class Case1(konst a: Int)  {
    var isCompared = false
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun compareTo(other: Case1):Any = run{
        TODO()
    }
}

fun case1() {
    konst a3 = Case1(-1)
    konst a4 = Case1(-3)

    konst x0 = a3 > a4
    konst x1 = a3 < a4
    konst x2 = a3 >= a4
    konst x3 = a3 <= a4
}


// TESTCASE NUMBER: 2
class Case2(konst a: Int) {
    var isCompared = false
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun compareTo(other: Case2): Nothing = run {
        TODO()
    }
}

fun case2() {
    konst a3 = Case2(-1)
    konst a4 = Case2(-3)

    konst x0 = a3 > a4
    konst x1 = a3 < a4
    konst x2 = a3 >= a4
    konst x3 = a3 <= a4
}


// TESTCASE NUMBER: 3
class Case3(konst a: Int) {
    var isCompared = false
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun compareTo(other: Case3):Long = run{
        TODO()
    }
}

fun case3() {
    konst a3 = Case3(-1)
    konst a4 = Case3(-3)

    konst x0 = a3 > a4
    konst x1 = a3 < a4
    konst x2 = a3 >= a4
    konst x3 = a3 <= a4
}

// TESTCASE NUMBER: 4
class Case4(konst a: Int) {
    var isCompared = false
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun compareTo(other: Case4):Int? = run{
        TODO()
    }
}

fun case4() {
    konst a3 = Case4(-1)
    konst a4 = Case4(-3)

    konst x0 = a3 > a4
    konst x1 = a3 < a4
    konst x2 = a3 >= a4
    konst x3 = a3 <= a4
}
