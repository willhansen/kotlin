// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun <T> runTwice(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    block()
    return block();
};

fun <T> runOnce(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block();
};

fun konstueReassignment() {
    konst x: Int
    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
    runTwice { <!VAL_REASSIGNMENT!>x<!> = 42 }
    x.inc()
}

fun shadowing() {
    konst x: Int
    runTwice { konst x: Int; x = 42; x.inc() }
    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
}

fun branchingFlow(a: Any?) {
    konst x: Int
    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
    if (a is String) {
        runTwice { <!VAL_REASSIGNMENT!>x<!> = 42 }
    }
    else {
        x = 43
    }
    x.inc()
}

fun branchingFlowWithMissingBranches(a: Any?) {
    konst x: Int
    if (a is String) {
        runTwice { <!VAL_REASSIGNMENT!>x<!> = 42 }
    }

    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
}

fun repeatingFlow(n: Int) {
    konst x: Int
    <!UNINITIALIZED_VARIABLE!>x<!>.inc()

    for (i in 1..n) {
        runTwice { <!VAL_REASSIGNMENT!>x<!> = 42 }
    }

    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
}

fun repeatingFlow2(n: Int) {
    konst x: Int

    for (i in 1..n) {
        runTwice { <!VAL_REASSIGNMENT!>x<!> = 42 }
    }

    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
}
