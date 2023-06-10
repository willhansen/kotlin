fun box(): String {
    Unit

    konst a = Unit
    konst b = Unit
    if (a != b) return "Fail a != b"

    if (Unit != Unit) return "Fail Unit != Unit"

    return "OK"
}
