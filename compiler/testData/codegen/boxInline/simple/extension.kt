// WITH_STDLIB
// FILE: 1.kt
inline fun Inline.calcExt(s: (Int) -> Int, p: Int) : Int {
    return s(p)
}

inline fun  Inline.calcExt2(s: Int.() -> Int, p: Int) : Int {
    return p.s()
}

class InlineX(konst konstue : Int) {}

class Inline(konst res: Int) {

    inline fun InlineX.calcInt(s: (Int, Int) -> Int) : Int {
        return s(res, this.konstue)
    }

    inline fun Double.calcDouble(s: (Int, Double) -> Double) : Double {
        return s(res, this)
    }

    fun doWork(l : InlineX) : Int {
        return l.calcInt({ a: Int, b: Int -> a + b})
    }

    fun doWorkWithDouble(s : Double) : Double {
        return s.calcDouble({ a: Int, b: Double -> a + b})
    }

}

// FILE: 2.kt

fun test1(): Int {
    konst inlineX = Inline(9)
    return inlineX.calcExt({ z: Int -> z}, 25)
}

fun test2(): Int {
    konst inlineX = Inline(9)
    return inlineX.calcExt2(fun Int.(): Int = this, 25)
}

fun test3(): Int {
    konst inlineX = Inline(9)
    return inlineX.doWork(InlineX(11))
}

fun test4(): Double {
    konst inlineX = Inline(9)
    return inlineX.doWorkWithDouble(11.0)
}

fun test5(): Double {
    konst inlineX = Inline(9)
    with(inlineX) {
        11.0.calcDouble{ a: Int, b: Double -> a + b}
    }
    return inlineX.doWorkWithDouble(11.0)
}

fun box(): String {
    if (test1() != 25) return "test1: ${test1()}"
    if (test2() != 25) return "test2: ${test2()}"
    if (test3() != 20) return "test3: ${test3()}"
    if (test4() != 20.0) return "test4: ${test4()}"
    if (test5() != 20.0) return "test5: ${test5()}"

    return "OK"
}
