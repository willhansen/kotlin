// IGNORE_BACKEND_K2: JVM_IR
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// !IGNORE_ERRORS

fun test1() {
    break
    continue
}

fun test2() {
    L1@ while (true) {
        break@ERROR
        continue@ERROR
    }
}

fun test3() {
    L1@ while (true) {
        konst lambda = {
            break@L1
            continue@L1
        }
    }
}

fun test4() {
    while (break) {}
    while (continue) {}
}
