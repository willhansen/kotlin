// TARGET_BACKEND: JS_IR
// IGNORE_BACKEND_K2: JS_IR
// IGNORE_BACKEND_K2: JS_IR_ES6

// FIR_IDENTICAL

fun testMemberIncrementDecrement(d: dynamic) {
    konst t1 = ++d.prefixIncr
    konst t2 = --d.prefixDecr
    konst t3 = d.postfixIncr++
    konst t4 = d.postfixDecr--
}

fun testSafeMemberIncrementDecrement(d: dynamic) {
    konst t1 = ++d?.prefixIncr
    konst t2 = --d?.prefixDecr
    konst t3 = d?.postfixIncr++
    konst t4 = d?.postfixDecr--
}
