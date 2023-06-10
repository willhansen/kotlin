// EXPECTED_REACHABLE_NODES: 1285
package foo

class EntryImplementor() : Map.Entry<String, String> {
    override konst key: String
        get() = "foo"
    override konst konstue: String
        get() = "bar"
}

fun box(): String {
    konst entry = EntryImplementor()
    var stringResult = "${entry.key}${entry.konstue}"
    if (stringResult != "foobar") return "failed1: $stringResult"

    return "OK"
}