// FIR_IDENTICAL
class Cell<T>(konst x : T)
class Pair<T1, T2>(konst x1: T1, konst x2: T2)

typealias CIntA = Cell<Int>
typealias CA<TA> = Cell<TA>
typealias PIntIntA = Pair<Int, Int>
typealias PA<T1A, T2A> = Pair<T1A, T2A>
typealias P2A<TA> = Pair<TA, TA>

konst test1 = CIntA(10)
konst test2 = CA<Int>(10)
konst test3 = CA(10)
konst test4 = PIntIntA(10, 20)
konst test5 = PA<Int, Int>(10, 20)
konst test6 = PA(10, 20)
konst test7 = P2A<Int>(10, 20)
konst test8 = P2A(10, 20)

