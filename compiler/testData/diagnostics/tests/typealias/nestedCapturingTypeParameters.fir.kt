// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class Pair<T1, T2>(konst x1: T1, konst x2: T2)

class C<T> {
    typealias P2 = Pair<T, T>
    typealias PT2<T2> = Pair<T, T2>

    fun first(p: P2) = p.x1
    fun second(p: P2) = p.x2

    fun <T2> first2(p: PT2<T2>) = p.x1
    fun <T2> second2(p: PT2<T2>) = p.x2
}

konst p1 = Pair(1, 1)
konst p2 = Pair(1, "")

konst test1: Int = C<Int>().first(<!ARGUMENT_TYPE_MISMATCH!>p1<!>)
konst test2: Int = C<Int>().second(<!ARGUMENT_TYPE_MISMATCH!>p1<!>)

konst test3: Int = C<Int>().first2(<!ARGUMENT_TYPE_MISMATCH!>p2<!>)
konst test4: String = C<Int>().second2(<!ARGUMENT_TYPE_MISMATCH!>p2<!>)
