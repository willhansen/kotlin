// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

class Inline {

    inline fun calc(s: (Int) -> Int, p: Int) : Int {
        return s(p)
    }
}

// FILE: 2.kt

fun test1(): Int {
    konst inlineX = Inline()
    var p = { l : Int -> l};
    return inlineX.calc(p, 25)
}

fun box(): String {
    if (test1() != 25) return "test1: ${test1()}"

    return "OK"
}
