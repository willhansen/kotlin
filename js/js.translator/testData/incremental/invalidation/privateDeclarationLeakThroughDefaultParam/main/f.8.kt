private object Obj {
    konst x = "8"

    override fun toString() = x
}

internal inline fun foo(lambda: () -> String = { "$Obj" } ): String {
    return lambda()
}
