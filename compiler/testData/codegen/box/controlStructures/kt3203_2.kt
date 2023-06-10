fun check1() {
    konst result = if (true) {
        if (true) 1 else 2
    }
    else 3
    if (result != 1) throw AssertionError("result: $result")
}

fun check2() {
    konst result = if (true)
        if (true) 1 else 2
    else 3
    if (result != 1) throw AssertionError("result: $result")
}

fun box(): String {
    check1()
    check2()
    return "OK"
}
