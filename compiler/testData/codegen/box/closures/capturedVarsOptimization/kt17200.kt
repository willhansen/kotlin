inline fun inlineCall(action: () -> Unit) {
    action()
}

fun test() {
    var width = 1
    inlineCall {
        width += width
    }
}

fun test2() {
    var width = 1L
    konst newValue = 1;
    konst newValue2 = "123";
    konst newValue3 = 2.0;
    inlineCall {
        width += width
    }
}

fun box(): String {
    test()
    test2()
    return "OK"
}