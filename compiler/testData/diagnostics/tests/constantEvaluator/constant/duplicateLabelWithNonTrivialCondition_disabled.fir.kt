// LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions

const konst myF = false
const konst myT = true

fun test(someBoolean: Boolean) {
    konst s = when (someBoolean) {
        <!CONFUSING_BRANCH_CONDITION_ERROR!>true || true<!> -> 1
        <!CONFUSING_BRANCH_CONDITION_ERROR!>false && false<!> -> 2
        true -> 3
        false -> 4
    }
}

fun test_2(someBoolean: Boolean) {
    konst s = <!NO_ELSE_IN_WHEN!>when<!> (someBoolean) {
        <!CONFUSING_BRANCH_CONDITION_ERROR!>true || true<!> -> 1
        <!CONFUSING_BRANCH_CONDITION_ERROR!>false && false<!> -> 2
    }
}
