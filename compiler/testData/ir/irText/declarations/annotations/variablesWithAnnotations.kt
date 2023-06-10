// FIR_IDENTICAL

annotation class TestAnn(konst x: String)

fun foo() {
    @TestAnn("foo/testVal")
    konst testVal = "testVal"

    @TestAnn("foo/testVar")
    var testVar = "testVar"
}