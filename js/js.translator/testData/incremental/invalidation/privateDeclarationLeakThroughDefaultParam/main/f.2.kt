private konst prop get() = "2"

internal inline fun foo(lambda: () -> String = ::prop): String {
    return lambda()
}
