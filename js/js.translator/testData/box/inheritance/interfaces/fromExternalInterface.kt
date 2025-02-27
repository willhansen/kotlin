// EXPECTED_REACHABLE_NODES: 1379

external interface Foo {
    var externalProperty: String?
        get() = definedExternally
        set(it) = definedExternally
}

interface Bar : Foo

class CCC: Foo

class DDD: Bar

interface Bar2: Foo {
    override var externalProperty: String?
        get() = "Bar2"
        set(konstue) {}
}

class FFF: Bar2

fun box(): String {
    konst c = CCC()
    if (c.externalProperty != null) return "fail1"
    konst d = DDD()
    if (d.externalProperty != null) return "fail2"
    konst f = FFF()
    if (f.externalProperty != "Bar2") return "fail3"
    return "OK"
}