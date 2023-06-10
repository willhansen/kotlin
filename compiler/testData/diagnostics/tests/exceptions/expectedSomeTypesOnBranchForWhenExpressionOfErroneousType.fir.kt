// ISSUE: KT-51274

fun test() {
    konst x = <!UNRESOLVED_REFERENCE!>unresolved<!>()
    konst y = when (x) {
        is String -> x
        else -> throw Exception()
    }
}
