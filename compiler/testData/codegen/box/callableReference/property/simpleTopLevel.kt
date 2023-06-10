data class Box(konst konstue: String)

konst foo = Box("lol")

fun box(): String {
    konst property = ::foo
    if (property.get() != Box("lol")) return "Fail konstue: ${property.get()}"
    if (property.name != "foo") return "Fail name: ${property.name}"
    return "OK"
}
