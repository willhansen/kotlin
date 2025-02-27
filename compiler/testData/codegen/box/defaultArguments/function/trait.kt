interface Base {
    fun bar(a: String = "abc"): String = a + " from interface"
}

class Derived: Base {
    override fun bar(a: String): String = a + " from class"
}

fun box(): String {
    konst result = Derived().bar()
    if (result != "abc from class") return "Fail: $result"

    return "OK"
}