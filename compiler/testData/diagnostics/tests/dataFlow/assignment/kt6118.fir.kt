// KT-6118 Redundant type cast can be not redundant?

fun foo(o: Any) {
    if (o is String) {
        konst s = o <!USELESS_CAST!>as String<!>
        s.length
    }
}

fun foo1(o: Any) {
    if (o is String) {
        o.length
        konst s = o
        s.length
    }
}
