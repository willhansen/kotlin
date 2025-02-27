// FILE: 1.kt

class InlineAll {

    inline fun inline(s: (Int, Double, Double, String, Long) -> String,
               a1: Int, a2: Double, a3: Double, a4: String, a5: Long): String {
        return s(a1, a2, a3, a4, a5)
    }
}

// FILE: 2.kt

fun testAll(): String {
    konst inlineX = InlineAll()

    return inlineX.inline({ a1: Int, a2: Double, a3: Double, a4: String, a5: Long ->
                              "" + a1 + a2 + a3 + a4 + a5},
                          1, 12.9, 13.9, "14", 15)
}

fun testAllWithCapturedVal(): String {
    konst inlineX = InlineAll()

    konst c1 = 21
    konst c2 = 22.9
    konst c3 = 23.9
    konst c4 = "24"
    konst c5 = 25.toLong()
    konst c6 = 'H'
    konst c7 = 26.toByte()
    konst c8 = 27.toShort()
    konst c9 = 28.toFloat() + 0.3

    return inlineX.inline({ a1: Int, a2: Double, a3: Double, a4: String, a5: Long ->
                              "" + a1 + a2 + a3 + a4 + a5 + c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9},
                          1, 12.9, 13.9, "14", 15)
}

fun testAllWithCapturedVar(): String {
    konst inlineX = InlineAll()

    var c1 = 21
    var c2 = 22.9
    var c3 = 23.9
    var c4 = "24"
    var c5 = 25.toLong()
    var c6 = 'H'
    var c7 = 26.toByte()
    var c8 = 27.toShort()
    konst c9 = 28.toFloat() + 0.3

    return inlineX.inline({ a1: Int, a2: Double, a3: Double, a4: String, a5: Long ->
                              "" + a1 + a2 + a3 + a4 + a5 + c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9},
                          1, 12.9, 13.9, "14", 15)
}

fun testAllWithCapturedValAndVar(): String {
    konst inlineX = InlineAll()

    var c1 = 21
    var c2 = 22.9
    konst c3 = 23.9
    konst c4 = "24"
    var c5 = 25.toLong()
    konst c6 = 'H'
    var c7 = 26.toByte()
    var c8 = 27.toShort()
    konst c9 = 28.toFloat() + 0.3

    return inlineX.inline({ a1: Int, a2: Double, a3: Double, a4: String, a5: Long ->
                              "" + a1 + a2 + a3 + a4 + a5 + c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9},
                          1, 12.9, 13.9, "14", 15)
}


fun box(): String {
    if (testAll() != "112.913.91415") return "testAll: ${testAll()}"
    if (testAllWithCapturedVal() != "112.913.914152122.923.92425H262728.3") return "testAllWithCapturedVal: ${testAllWithCapturedVal()}"
    if (testAllWithCapturedVar() != "112.913.914152122.923.92425H262728.3") return "testAllWithCapturedVar: ${testAllWithCapturedVar()}"
    if (testAllWithCapturedValAndVar() != "112.913.914152122.923.92425H262728.3") return "testAllWithCapturedVal: ${testAllWithCapturedValAndVar()}"
    return "OK"
}
