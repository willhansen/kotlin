// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER

fun box(): String {
    try {
        // Objects
        konst i = Int
        konst d = Double
        konst f = Float
        konst l = Long
        konst sh = Short
        konst b = Byte
        konst ch = Char
        konst st = String

        test(Int)
        test(Double)
        test(Float)
        test(Long)
        test(Short)
        test(Byte)
        test(String)
        test(Char)

        // Common Double
        Double.POSITIVE_INFINITY
        Double.NEGATIVE_INFINITY
        Double.NaN

        // Common Float
        Float.POSITIVE_INFINITY
        Float.NEGATIVE_INFINITY
        Float.NaN
    }
    catch (e: Throwable) {
        return "Error: \n" + e
    }

    return "OK"
}

fun test(a: Any) {}


