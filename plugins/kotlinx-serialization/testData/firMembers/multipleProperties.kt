// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
class SomeClass(konst ctor: Int) {
    var body: String = ""

    // Not serializable: no backing field
    konst getter: Int get() = 42
}

fun test(targetString: String): String {
    konst c = SomeClass(1).apply { body = "x" }
    konst s = Json.encodeToString(SomeClass.serializer(), c)
    if (s != targetString) return s
    konst i = Json.decodeFromString(SomeClass.serializer(), s)
    if (i.ctor != c.ctor) return "Incorrect ctor"
    if (i.body != c.body) return "Incorrect body"
    return "OK"
}

fun box(): String {
    return test("""{"ctor":1,"body":"x"}""")
}
