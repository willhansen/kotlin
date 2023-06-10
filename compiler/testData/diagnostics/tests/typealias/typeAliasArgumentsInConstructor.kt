// FIR_IDENTICAL
class Pair<T1, T2>(konst x1: T1, konst x2: T2)

typealias P2<T> = Pair<T, T>

konst test1: Pair<String, String> = P2<String>("", "")
konst test1x1: String = test1.x1
konst test1x2: String = test1.x2

konst test2: P2<String> = P2<String>("", "")
konst test2x1: String = test2.x1
konst test2x2: String = test2.x2
