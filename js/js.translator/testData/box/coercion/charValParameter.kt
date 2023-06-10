// EXPECTED_REACHABLE_NODES: 1276

class A(@JsName("x") konst x: Char)

fun typeOf(x: dynamic): String = js("typeof x")

konst expectedCharRepresentationInProperty = if (testUtils.isLegacyBackend()) "object" else "number"

fun box(): String {
    konst a = A('0')

    var r = typeOf(a.asDynamic().x)
    if (r != expectedCharRepresentationInProperty) return "fail1: $r"

    r = typeOf(a.x)
    if (r != "number") return "fail2: $r"

    return "OK"
}