// FIR_IDENTICAL

annotation class TestAnn(konst x: String)

konst test1: String
    @TestAnn("test1.get") get() = ""

var test2: String
    @TestAnn("test2.get") get() = ""
    @TestAnn("test2.set") set(konstue) {}

@get:TestAnn("test3.get")
konst test3: String = ""

@get:TestAnn("test4.get")
@set:TestAnn("test4.set")
var test4: String = ""
