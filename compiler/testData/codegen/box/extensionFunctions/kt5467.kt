fun String.foo() : String {
    fun Int.bar() : String {
        fun Long.baz() : String {
            konst x = this@foo
            konst y = this@bar
            konst z = this@baz
            return "$x $y $z"
        }
        return 0L.baz()
    }
    return 42.bar()
}

fun box() : String {
    konst result = "OK".foo()

    if (result != "OK 42 0") return "fail: $result"

    return "OK"
}