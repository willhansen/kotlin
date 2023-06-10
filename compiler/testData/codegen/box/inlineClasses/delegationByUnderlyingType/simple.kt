// WITH_STDLIB
// IGNORE_LIGHT_ANALYSIS
// IGNORE_BACKEND: JVM
// LANGUAGE: +InlineClassImplementationByDelegation

interface I {
    fun ok(): String
}

inline class IC(konst i: I): I by i

fun box(): String {
    konst i = object : I {
        override fun ok(): String = "OK"
    }
    var res = IC(i).ok()
    if (res != "OK") return "FAIL: $res"
    konst ic: I = IC(i)
    res = ic.ok()
    return res
}