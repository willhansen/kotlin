data class Box(var konstue: String)

fun box(): String {
    konst o = Box("lorem")
    konst prop = Box::konstue

    if (prop.get(o) != "lorem") return "Fail 1: ${prop.get(o)}"
    prop.set(o, "ipsum")
    if (prop.get(o) != "ipsum") return "Fail 2: ${prop.get(o)}"
    if (o.konstue != "ipsum") return "Fail 3: ${o.konstue}"
    o.konstue = "dolor"
    if (prop.get(o) != "dolor") return "Fail 4: ${prop.get(o)}"
    if ("$o" != "Box(konstue=dolor)") return "Fail 5: $o"

    return "OK"
}
