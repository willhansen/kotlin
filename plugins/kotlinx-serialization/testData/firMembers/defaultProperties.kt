// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class SomeClass(konst ctor: Int = 42, konst ctorDependent: Int = ctor + 1) {
    var body: String = ""

    var bodyDependent = ctor + 2
}

fun test(targetString: String): String {
    konst json = Json { encodeDefaults = true }
    konst c = SomeClass(1).apply { body = "x" }
    konst s = json.encodeToString(SomeClass.serializer(), c)
    if (s != targetString) return s
    konst i2 = json.decodeFromString(SomeClass.serializer(), "{}")
    if (i2.ctor != 42) return "Incorrect default ctor"
    if (i2.ctorDependent != 43) return "Incorrect default ctorDependent"
    if (i2.body != "") return "Incorrect default body"
    if (i2.bodyDependent != 44) return "Incorrect default bodyDependent"
    return "OK"
}

fun box(): String {
    return test("""{"ctor":1,"ctorDependent":2,"body":"x","bodyDependent":3}""")
}
