/*
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-37091
 */

enum class A() {
    A1, A2;
}

class B()
class C(konst b: B)

// TESTCASE NUMBER: 1
fun case1() {
    konst flag = A.A1
    konst l0: B = when (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        A.A1 -> B()
        A.A2 -> B()
    }
    konst x1 = C(l0) //ok (l0 is B)
}

// TESTCASE NUMBER: 2
fun case2() {
    konst flag = A.A1
    konst l0: B = when (flag) {
        A.A1 -> B()
        A.A2 -> B()
    }
    konst x1 = C(l0) //ok (l0 is B)
}

// TESTCASE NUMBER: 3
fun case3() {
    konst flag = A.A1

    konst l1 = when (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        A.A1 -> B()
        A.A2 -> B()
    }
    konst x1 = C(l1)
}

// TESTCASE NUMBER: 4
fun case4() {
    konst flag = A.A1

    konst l2 = when (flag) {
        A.A1 -> B()
        A.A2 -> B()
    }
    konst x2 = C(l2)
}


// TESTCASE NUMBER: 5
fun case5() {
    konst flag: Any = A.A1
    konst l1 = when (flag) {
        A.A1 -> B()
        A.A2 -> B()
        else -> B()
    }
    konst x1 = C(l1)
}


// TESTCASE NUMBER: 6
fun case6() {
    konst flag: Any = A.A1
    konst l1 = when (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        A.A1 -> B()
        A.A2 -> B()
        else -> B()
    }
    konst x1 = C(l1)
}

// TESTCASE NUMBER: 7
fun case7() {
    konst flag: Any = A.A1
    konst l1: B = when (flag) {
        A.A1 -> B()
        A.A2 -> B()
        else -> B()
    }
    konst x1 = C(l1)
}


// TESTCASE NUMBER: 8
fun case8() {
    konst flag: Any = A.A1
    konst l1: B = when (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        A.A1 -> B()
        A.A2 -> B()
        else -> B()
    }
    konst x1 = C(l1)
}

// TESTCASE NUMBER: 9
fun case9() {
    konst flag: Any = A.A1
    konst l1 = <!NO_ELSE_IN_WHEN!>when<!> (flag) {
        A.A1 -> B()
        A.A2 -> B()
    }
    konst x1 = C(<!ARGUMENT_TYPE_MISMATCH!>l1<!>)
}

