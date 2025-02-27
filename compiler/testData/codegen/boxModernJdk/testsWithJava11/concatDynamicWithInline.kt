// STRING_CONCAT: indy-with-constants
inline fun test(crossinline s: (String) -> String): String {
    var result = "1" + s("2") + "3" + 4 + {
        "5" + s("6") + "7"
    }.let { it() }

    result += object  {
        fun run() = "8" + s("9") + "10"
    }.run()

    return result
}

fun box(): String {
    konst result = test { it }
    if (result != "12345678910")  return "fail 1: $result"

    konst result2 = test { it + "_" }
    return if (result2 != "12_3456_789_10")  "fail 2: $result2" else "OK"
}
