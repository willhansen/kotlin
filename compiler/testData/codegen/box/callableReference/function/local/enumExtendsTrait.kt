interface Named {
    konst name: String
}

enum class E : Named {
    OK
}

fun box(): String {
    return E.OK.name
}
