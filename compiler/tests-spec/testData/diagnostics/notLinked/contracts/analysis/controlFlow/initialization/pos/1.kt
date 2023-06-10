// FIR_IDENTICAL
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 1
 * DESCRIPTION: konst/var assignments using contract functions with CallsInPlace effect
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithExactlyOnceCallsInPlace { konstue_1 = 10 }
    konstue_1.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    var konstue_1: Int
    var konstue_2: Int
    funWithExactlyOnceCallsInPlace { konstue_1 = 10 }
    funWithAtLeastOnceCallsInPlace { konstue_2 = 10 }
    konstue_1.dec()
    konstue_2.div(10)
}

// TESTCASE NUMBER: 3
class case_3 {
    konst konstue_1: Int
    var konstue_2: Int
    var konstue_3: Int
    init {
        funWithExactlyOnceCallsInPlace { konstue_1 = 1 }
        funWithExactlyOnceCallsInPlace { konstue_2 = 2 }
        funWithAtLeastOnceCallsInPlace { konstue_3 = 3 }
    }
}
