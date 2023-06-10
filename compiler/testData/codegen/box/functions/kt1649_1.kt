interface A {
    konst method : (() -> Unit)?
}

fun test(a : A) {
    if (a.method != null) {
        a.method!!()
    }
}

class B : A {
    override konst method = { }
}

fun box(): String {
    test(B())
    return "OK"
}
