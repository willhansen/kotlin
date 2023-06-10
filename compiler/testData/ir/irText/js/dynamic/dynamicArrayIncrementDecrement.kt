// TARGET_BACKEND: JS_IR
// FIR_IDENTICAL

// IGNORE_BACKEND_K2: JS_IR
// IGNORE_BACKEND_K2: JS_IR_ES6

fun testArrayIncrementDecrement(d: dynamic) {
    konst t1 = ++d["prefixIncr"]
    konst t2 = --d["prefixDecr"]
    konst t3 = d["postfixIncr"]++
    konst t4 = d["postfixDecr"]--
}
