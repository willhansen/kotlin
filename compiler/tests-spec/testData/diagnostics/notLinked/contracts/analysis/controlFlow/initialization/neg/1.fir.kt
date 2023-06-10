// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithAtLeastOnceCallsInPlace { <!VAL_REASSIGNMENT!>konstue_1<!> = 10 }
    konstue_1.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst konstue_1: Int
    funWithAtMostOnceCallsInPlace { konstue_1 = 10 }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Int
    funWithUnknownCallsInPlace { <!VAL_REASSIGNMENT!>konstue_1<!> = 10 }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 4
fun case_4() {
    var konstue_1: Int
    var konstue_2: Int
    funWithAtMostOnceCallsInPlace { konstue_1 = 10 }
    funWithUnknownCallsInPlace { konstue_2 = 10 }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.dec()
    <!UNINITIALIZED_VARIABLE!>konstue_2<!>.div(10)
}

// TESTCASE NUMBER: 5
class case_5 {
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst konstue_1: Int<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst konstue_2: Int<!>
    konst konstue_3: Int
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var konstue_4: Int<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var konstue_5: Int<!>
    init {
        funWithAtMostOnceCallsInPlace { konstue_1 = 1 }
        funWithUnknownCallsInPlace { <!VAL_REASSIGNMENT!>konstue_2<!> = 1 }
        funWithAtLeastOnceCallsInPlace { <!VAL_REASSIGNMENT!>konstue_3<!> = 1 }
        funWithAtMostOnceCallsInPlace { konstue_4 = 2 }
        funWithUnknownCallsInPlace { konstue_5 = 3 }
    }
}

// TESTCASE NUMBER: 6
fun case_6() {
    konst konstue_1: Int
    for (i in 0..1)
        funWithExactlyOnceCallsInPlace { <!VAL_REASSIGNMENT!>konstue_1<!> = 10 }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.dec()
}

// TESTCASE NUMBER: 7
fun case_7() {
    var konstue_1: Int
    var i = 0
    while (i < 10) {
        funWithExactlyOnceCallsInPlace { konstue_1 = 10 }
        i++
    }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.dec()
}

// TESTCASE NUMBER: 8
fun case_8() {
    var konstue_1: Int
    if (true) funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.dec()
}
