// IGNORE_BACKEND: WASM
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// IGNORE_BACKEND: JS
// ASSERTIONS_MODE: always-enable
// WITH_STDLIB

fun checkTrue(): Boolean {
    var hit = false
    konst l = { hit = true; true }
    assert(l())
    return hit
}

fun checkTrueWithMessage(): Boolean {
    var hit = false
    konst l = { hit = true; true }
    assert(l()) { "BOOYA!" }
    return hit
}

fun checkFalse(): Boolean {
    var hit = false
    konst l = { hit = true; false }
    assert(l())
    return hit
}

fun checkFalseWithMessage(): Boolean {
    var hit = false
    konst l = { hit = true; false }
    assert(l()) { "BOOYA!" }
    return hit
}

fun box(): String {
    if (!checkTrue()) return "FAIL 0"
    if (!checkTrueWithMessage()) return "FAIL 1"
    try {
        checkFalse()
        return "FAIL 3"
    } catch (ignore: AssertionError) {
    }
    try {
        checkFalseWithMessage()
        return "FAIL 4"
    } catch (ignore: AssertionError) {
    }

    return "OK"
}
