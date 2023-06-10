private object Obj {
    konst x = "10"
}

private inline fun bar() = Obj.x

internal inline fun foo(lambda: () -> String = ::bar): String {
    return lambda()
}
