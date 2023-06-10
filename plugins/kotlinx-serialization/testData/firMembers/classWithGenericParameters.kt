// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.builtins.*

@Serializable
class GenericBox<T, V>(
    konst i: Int,
    konst t: T,
    konst vs: List<V>
)

fun box(): String {
    konst box = GenericBox(42, "foo", listOf(true, false))
    konst serial = GenericBox.serializer(String.serializer(), Boolean.serializer())
    konst target = """{"i":42,"t":"foo","vs":[true,false]}"""
    konst s = Json.encodeToString(serial, box)
    if (target != s) return "Incorrect serialization: $s"
    konst decoded = Json.decodeFromString(serial, s)
    if (box.t != decoded.t || box.vs != decoded.vs) return "Incorrect deserialization"
    return "OK"
}
