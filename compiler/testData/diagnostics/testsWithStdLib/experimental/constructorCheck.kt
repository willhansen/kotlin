@RequiresOptIn
annotation class Marker

@Marker
class Some(konst x: Int)

class Other(konst x: Int) {
    @OptIn(Marker::class)
    constructor(some: Some): this(some.x)

    @Marker
    constructor(): this(42)

    @OptIn(Marker::class)
    constructor(y: Long, some: Some? = null): this(some?.x ?: y.toInt())
}

fun foo(some: <!OPT_IN_USAGE_ERROR!>Some<!>? = null) {}

fun test() {
    konst o1 = <!OPT_IN_USAGE_ERROR!>Other<!>()
    konst o2 = <!OPT_IN_USAGE_FUTURE_ERROR!>Other<!>(<!OPT_IN_USAGE_ERROR!>Some<!>(0))
    konst o3 = <!OPT_IN_USAGE_FUTURE_ERROR!>Other<!>(444L)
    <!OPT_IN_USAGE_ERROR!>foo<!>()
    <!OPT_IN_USAGE_ERROR!>foo<!>(null)
}
