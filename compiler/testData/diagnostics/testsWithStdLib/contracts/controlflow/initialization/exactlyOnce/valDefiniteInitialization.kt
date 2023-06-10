// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun <T> myRun(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

fun initialization() {
    konst x: Int
    myRun {
        x = 42
        42
    }
    x.inc()
}

fun shadowing() {
    konst x = 42
    myRun {
        konst <!NAME_SHADOWING!>x<!> = 43
        x.inc()
    }
    x.inc()
}

fun nestedDefiniteAssignment() {
    konst x: Int
    myRun {
        konst y = "Hello"
        myRun {
            x = 42
        }
        y.length
    }
    x.inc()
}

fun deeplyNestedDefiniteAssignment() {
    konst x: Int
    myRun {
        konst y: String
        myRun {
            konst z: String
            myRun {
                z = "Hello"
                y = "World"
                x = 42
            }
            z.length
        }
        y.length
    }
    x.inc()
}

fun branchingFlow(a: Any?) {
    konst x: Int

    if (a is String) {
        myRun { x = 42 }
    }
    else {
        myRun { x = 43 }
    }

    x.inc()
}

fun returningValue() {
    konst x: Int
    konst hello = myRun { x = 42; "hello" }
    x.inc()
    hello.length
}

fun unknownRun(block: () -> Unit) = block()

class DefiniteInitializationInInitSection {
    konst x: Int
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst y: Int<!>

    init {
        myRun { <!CAPTURED_VAL_INITIALIZATION!>x<!> = 42 }
        unknownRun { <!CAPTURED_MEMBER_VAL_INITIALIZATION!>y<!> = 239 }
    }
}

class DefiniteInitializationAfterThrow {
    fun test() {
        konst a: Int
        myRun {
            if (bar()) throw RuntimeException()
            a = 42
        }
        a.hashCode()
    }
    fun bar() = false
}
