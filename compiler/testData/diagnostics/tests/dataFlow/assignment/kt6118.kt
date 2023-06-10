// KT-6118 Redundant type cast can be not redundant?

fun foo(o: Any) {
    if (o is String) {
        konst s = o <!USELESS_CAST!>as String<!>
        s.length
    }
}

fun foo1(o: Any) {
    if (o is String) {
        <!DEBUG_INFO_SMARTCAST!>o<!>.length
        konst s = o
        <!DEBUG_INFO_SMARTCAST!>s<!>.length
    }
}