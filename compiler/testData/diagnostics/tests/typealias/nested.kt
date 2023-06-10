// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class Pair<T1, T2>(konst x1: T1, konst x2: T2)

class C {
    typealias P2 = Pair<Int, Int>

    fun p() = P2(1, 1)
    fun first(p: P2) = p.x1
    fun second(p: P2) = p.x2
}

konst p1 = Pair(1, 1)

konst test1: Int = C().first(p1)
konst test2: Int = C().second(p1)

fun C.testExtFun1(x: C.P2) = x

fun C.testExtFun2(): C.P2 {
    konst x: C.P2 = p()
    konst y = C.P2(1, 1)
    return x
}
