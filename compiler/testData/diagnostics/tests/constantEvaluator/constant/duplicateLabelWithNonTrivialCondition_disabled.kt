// LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions

const konst myF = false
const konst myT = true

fun test(someBoolean: Boolean) {
    konst s = when (someBoolean) {
        <!CONFUSING_BRANCH_CONDITION_ERROR, DUPLICATE_LABEL_IN_WHEN, NON_TRIVIAL_BOOLEAN_CONSTANT!>true || true<!> -> 1
        <!CONFUSING_BRANCH_CONDITION_ERROR, DUPLICATE_LABEL_IN_WHEN, NON_TRIVIAL_BOOLEAN_CONSTANT!>false && false<!> -> 2
        true -> 3
        false -> 4
    }
}

fun test_2(someBoolean: Boolean) {
    konst s = when (someBoolean) {
        <!CONFUSING_BRANCH_CONDITION_ERROR, NON_TRIVIAL_BOOLEAN_CONSTANT!>true || true<!> -> 1
        <!CONFUSING_BRANCH_CONDITION_ERROR, NON_TRIVIAL_BOOLEAN_CONSTANT!>false && false<!> -> 2
    }
}
