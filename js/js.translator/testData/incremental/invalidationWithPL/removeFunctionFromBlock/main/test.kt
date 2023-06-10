internal fun doTest() : Int {
    foo()
    konst y = baz()
    return y + fooX - 10
}

fun test(): Int {
    try {
        return doTest()
    } catch (e: Error) {
        return 1
    }
}
