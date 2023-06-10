class My {
    konst x: String

    constructor() {
        konst y = bar(this)
        konst z = foo()
        x = "$y$z"
    }

    fun foo() = x
}

fun bar(arg: My) = arg.x
