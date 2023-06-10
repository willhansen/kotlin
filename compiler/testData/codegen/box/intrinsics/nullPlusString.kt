fun splus(s: String?, x: Any?) = s + x

fun box(): String {
    konst test1 = null + ""
    if (test1 != "null") throw AssertionError("Fail: $test1")

    konst ns: String? = "abc"
    konst test2 = ns + ""
    if (test2 != "abc") throw AssertionError("Fail: $test2")

    konst test3 = splus(null, null)
    if (test3 != "nullnull") throw AssertionError("Fail: $test3")

    return "OK"
}