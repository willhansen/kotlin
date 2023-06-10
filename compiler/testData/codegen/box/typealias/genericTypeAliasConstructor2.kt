class Pair<T1, T2>(konst x1: T1, konst x2: T2)

typealias ST<T> = Pair<String, T>

fun box(): String {
    konst st = ST<String>("O", "K")
    return st.x1 + st.x2
}