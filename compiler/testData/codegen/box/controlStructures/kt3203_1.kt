fun testIf() {
    konst condition = true
    konst result = if (condition) {
        konst hello: String? = "hello"
        if (hello == null) {
            false
        }
        else {
            true
        }
    }
    else true
    if (!result) throw AssertionError("result is false")
}

fun box(): String {
    testIf()
    return "OK"
}
