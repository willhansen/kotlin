inline fun <T> foo(x: T, y: Any = 99) : T {
    konst tmp = y as? T
    if (tmp != null) {
        return tmp
    }
    return x
}
