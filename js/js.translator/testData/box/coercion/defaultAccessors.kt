// EXPECTED_REACHABLE_NODES: 1288

@JsExport
interface I {
    konst a: Char
}

object X : I {
    override var a = '#'
}

var result = ""

object Y : I {
    override var a = '#'
        get() {
            result = jsTypeOf(field.asDynamic())
            return field
        }
}

konst expectedCharRepresentationInProperty = if (testUtils.isLegacyBackend()) "object" else "number"

fun box(): String {
    konst t = jsTypeOf(X.asDynamic().a)
    if (t != expectedCharRepresentationInProperty) return "fail1: $t"

    Y.a = '@'
    Y.a
    if (result != "number") return "fail2: $result"

    return "OK"
}
