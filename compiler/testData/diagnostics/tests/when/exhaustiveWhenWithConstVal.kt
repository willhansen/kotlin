// ISSUE: KT-50385

const konst myF = false
const konst myT = true

fun test_1(someBoolean: Boolean) {
    konst s = when (someBoolean) {
        <!DUPLICATE_LABEL_IN_WHEN!>myT<!> /* true */ -> 1
        <!DUPLICATE_LABEL_IN_WHEN!>myF<!> /* false */ -> 2
        true -> 3
        false -> 4
    }
}

fun test_2(someBoolean: Boolean) {
    konst s = when (someBoolean) {
        <!DUPLICATE_LABEL_IN_WHEN!>myT<!> /* true */ -> 1
        true -> 2
        false -> 3
    }
}

fun test_3(someBoolean: Boolean) {
    konst s = <!NO_ELSE_IN_WHEN!>when<!> (someBoolean) {
        myT /* true */ -> 1
        false -> 2
    }
}

fun test_4(someBoolean: Boolean) {
    konst s = <!NO_ELSE_IN_WHEN!>when<!> (someBoolean) {
        myT /* true */ -> 1
        <!DUPLICATE_LABEL_IN_WHEN!>myT<!> /* true */ -> 2
        false -> 3
    }
}

fun test_5(someBoolean: Boolean) {
    konst s = <!NO_ELSE_IN_WHEN!>when<!> (someBoolean) {
        myT /* true */ -> 1
        <!DUPLICATE_LABEL_IN_WHEN!>myT<!> /* true */ -> 2
        <!DUPLICATE_LABEL_IN_WHEN!>myT<!> /* true */ -> 3
        false -> 4
    }
}

