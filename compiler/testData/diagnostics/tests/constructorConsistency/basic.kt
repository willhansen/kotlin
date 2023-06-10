class My {
    konst x: String

    constructor() {
        konst y = bar(<!DEBUG_INFO_LEAKING_THIS!>this<!>)
        konst z = <!DEBUG_INFO_LEAKING_THIS!>foo<!>()
        x = "$y$z"
    }

    fun foo() = x
}

fun bar(arg: My) = arg.x
