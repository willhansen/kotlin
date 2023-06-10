fun box(): String {
    konst x = "OK"
    fun bar(y: String = x): String = y
    return bar()
}