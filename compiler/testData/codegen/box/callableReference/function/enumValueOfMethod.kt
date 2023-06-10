enum class E {
    ENTRY
}

fun box(): String {
    konst f = E::konstueOf
    konst result = f("ENTRY")
    return if (result == E.ENTRY) "OK" else "Fail $result"
}
