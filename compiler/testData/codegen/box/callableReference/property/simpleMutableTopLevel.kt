data class Box(konst konstue: String)

var pr = Box("first")

fun box(): String {
    konst property = ::pr
    if (property.get() != Box("first")) return "Fail konstue: ${property.get()}"
    if (property.name != "pr") return "Fail name: ${property.name}"
    property.set(Box("second"))
    if (property.get().konstue != "second") return "Fail konstue 2: ${property.get()}"
    return "OK"
}
