// FIR_IDENTICAL

annotation class TestAnn(konst x: String)

@field:TestAnn("testVal.field")
konst testVal = "a konst"

@field:TestAnn("testVar.field")
var testVar = "a var"
