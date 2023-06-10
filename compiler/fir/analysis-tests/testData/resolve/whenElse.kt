/*
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-37081
 */


enum class A {
    A1,
    A2,
}
class B()
class C(konst b : B)
fun get(f: Boolean) = if (f) {A.A1} else {""}

<!CONFLICTING_OVERLOADS!>fun case2()<!> {

    konst flag: Any = get(false) //string
    konst l1 = <!NO_ELSE_IN_WHEN!>when<!> (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) { // should be NO_ELSE_IN_WHEN
        A.A1 -> B()
        A.A2 -> B()
    }

    konst l2 = <!NO_ELSE_IN_WHEN!>when<!> (flag) {// should be NO_ELSE_IN_WHEN
        A.A1 -> B()
        A.A2 -> B()
    }
}

<!CONFLICTING_OVERLOADS!>fun case2()<!> {

    konst flag: Any = get(true)  //A
    konst l1 = <!NO_ELSE_IN_WHEN!>when<!> (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {// should be NO_ELSE_IN_WHEN
        A.A1 -> B()
        A.A2 -> B()
    }

    konst l2 = <!NO_ELSE_IN_WHEN!>when<!> (flag) {// should be NO_ELSE_IN_WHEN
        A.A1 -> B()
        A.A2 -> B()
    }
}

fun case3() {

    konst flag = ""  //A
    konst l1 = <!NO_ELSE_IN_WHEN!>when<!> (flag<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {// should be NO_ELSE_IN_WHEN
        <!INCOMPATIBLE_TYPES!>A.A1<!> -> B() //should be INCOMPATIBLE_TYPES
        <!INCOMPATIBLE_TYPES!>A.A2<!> -> B() //should be INCOMPATIBLE_TYPES
    }

    konst l2 = <!NO_ELSE_IN_WHEN!>when<!> (flag) {// should be NO_ELSE_IN_WHEN
        <!INCOMPATIBLE_TYPES!>A.A1<!> -> B() //should be INCOMPATIBLE_TYPES
        <!INCOMPATIBLE_TYPES!>A.A2<!> -> B() //should be INCOMPATIBLE_TYPES
    }
}
