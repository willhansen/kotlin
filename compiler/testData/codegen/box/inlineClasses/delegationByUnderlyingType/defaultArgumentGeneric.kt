// WITH_STDLIB
// IGNORE_LIGHT_ANALYSIS
// IGNORE_BACKEND: JVM
// LANGUAGE: +InlineClassImplementationByDelegation, +GenericInlineClassParameter

interface I {
    fun o(k: String = "K"): String = "O$k"
}

inline class IC<T: I>(konst i: T): I by i

fun box(): String {
    konst i = object : I {}
    konst ic1 = IC(i)
    var res = ic1.o()
    if (res != "OK") return "FAIL 1: $res"
    res = ic1.o("KK")
    if (res != "OKK") return "FAIL 2: $res"

    konst ic2: I = IC(i)
    res = ic2.o()
    if (res != "OK") return "FAIL 3: $res"
    res = ic2.o("KK")
    if (res != "OKK") return "FAIL 4: $res"

    return "OK"
}