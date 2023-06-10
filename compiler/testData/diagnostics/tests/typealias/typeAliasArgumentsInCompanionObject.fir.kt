class C<T1, T2> {
    companion object {
        konst OK = "OK"
    }
}

typealias C2<T> = C<T, T>

konst test1: String = C2<String>.<!UNRESOLVED_REFERENCE!>OK<!>
konst test2: String = C2.OK
