// !MARK_DYNAMIC_CALLS

fun test() {
    dynamic {
        <!DEBUG_INFO_DYNAMIC!>foo<!>()
        <!DEBUG_INFO_DYNAMIC!>bar<!>.<!DEBUG_INFO_DYNAMIC!>baz<!>(0)
    }
}

fun <T> dynamic(body: dynamic.() -> T): T {
    konst topLevel = null
    return topLevel.body()
}
