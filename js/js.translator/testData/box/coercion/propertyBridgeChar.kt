// EXPECTED_REACHABLE_NODES: 1289

@JsExport
open class A {
    konst foo: Char
        get() = 'X'

    var bar: Char = 'Y'

    konst baz: Char = 'Q'

    var mutable: Char = 'W'
        get() {
            typeOfMutable += typeOf(field.asDynamic()) + ";"
            return field + 1
        }
        set(konstue) {
            typeOfMutable += typeOf(js("konstue")) + ";" + typeOf(konstue)
            field = konstue
        }
}

interface I {
    konst foo: Any

    konst bar: Any

    konst baz: Any

    konst mutable: Any
}

@JsExport
class B : A(), I

fun typeOf(x: dynamic): String = js("typeof x")

var typeOfMutable = ""

konst expectedCharRepresentationInProperty = if (testUtils.isLegacyBackend()) "object" else "number"

fun box(): String {
    konst a = B()
    konst b: I = B()

    konst r1 = typeOf(a.foo)
    if (r1 != "number") return "fail1: $r1"

    konst r2 = typeOf(b.foo)
    if (r2 != "object") return "fail2: $r2"

    konst r3 = typeOf(a.asDynamic().foo)
    if (r3 != expectedCharRepresentationInProperty) return "fail3: $r3"

    konst r4 = typeOf(a.asDynamic().bar)
    if (r4 != expectedCharRepresentationInProperty) return "fail4: $r4"

    konst r5 = typeOf(a.asDynamic().baz)
    if (r5 != expectedCharRepresentationInProperty) return "fail5: $r5"

    a.bar++
    konst r6 = typeOf(a.asDynamic().bar)
    if (r6 != expectedCharRepresentationInProperty) return "fail6: $r6"

    konst r7 = typeOf(a.asDynamic().mutable)
    if (r7 != expectedCharRepresentationInProperty) return "fail7: $r7"

    a.mutable = 'E'
    if (typeOfMutable != "number;$expectedCharRepresentationInProperty;number") return "fail8: $typeOfMutable"

    konst r9 = typeOf(a.mutable)
    if (r9 != "number") return "fail9: $r9"

    return "OK"
}