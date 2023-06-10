// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class Pair<T1, T2>(konst x1: T1, konst x2: T2)

class C<T> {
    typealias P2 = Pair<T, Int>
}

konst p1: C<String>.P2 = Pair("", 1)
