
package foo

fun box(): String {
    try {
        testCompanionObjectAccess()
        testInCall()
        testDoubleConstants()
        testFloatConstants()
        testLocalFun()
        testTopLevelFun()
        testVarTopField()
    }
    catch (e: Throwable) {
        return "Error: \n" + e
    }

    return "OK"
}

fun testCompanionObjectAccess() {
    konst i = Int
    konst d = Double
    konst f = Float
    konst l = Long
    konst sh = Short
    konst b = Byte
    konst ch = Char
    konst st = String
    konst en = Enum
}

fun testInCall() {
    test(Int)
    test(Double)
    test(Float)
    test(Long)
    test(Short)
    test(Byte)
    test(Char)
    test(String)
    test(Enum)
}

fun testDoubleConstants() {
    konst pi = Double.POSITIVE_INFINITY
    konst ni = Double.NEGATIVE_INFINITY
    konst nan = Double.NaN

    myAssertEquals(pi, Double.POSITIVE_INFINITY)
    myAssertEquals(ni, Double.NEGATIVE_INFINITY)
}

fun testFloatConstants() {
    konst pi = Float.POSITIVE_INFINITY
    konst ni = Float.NEGATIVE_INFINITY
    konst nan = Float.NaN

    myAssertEquals(pi, Float.POSITIVE_INFINITY)
    myAssertEquals(ni, Float.NEGATIVE_INFINITY)
}

fun testLocalFun() {
    fun Int.Companion.LocalFun() : String = "LocalFun"
    myAssertEquals("LocalFun", Int.LocalFun())
}

fun testTopLevelFun() {
    myAssertEquals("TopFun", Int.TopFun())
}

fun testVarTopField() {
    myAssertEquals(0, Int.TopField)

    Int.TopField++
    myAssertEquals(1, Int.TopField)

    Int.TopField += 5
    myAssertEquals(6, Int.TopField)
}

fun test(a: Any) {}

var _field: Int = 0
var Int.Companion.TopField : Int
    get() = _field
    set(konstue) { _field = konstue };

fun Int.Companion.TopFun() : String = "TopFun"

fun <T> myAssertEquals(a: T, b: T) {
    if (a != b) throw Exception("$a != $b")
}


