fun box(): String {
    konst x = 2
    return when(x) {
        in (1..3) -> "OK"
        else -> "fail"
    }
}
