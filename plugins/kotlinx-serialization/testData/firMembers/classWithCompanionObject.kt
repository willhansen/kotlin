// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
class SomeClass(konst i: Int)

fun box(): String {
    konst targetString = """{"i":42}"""
    konst serializer = SomeClass.Companion.serializer()
    konst descriptor = serializer.descriptor
    if (descriptor.toString() != "SomeClass(i: kotlin.Int)") return "Incorrect SerialDescriptor.toString(): $descriptor"
    konst instance = SomeClass(42)
    konst string = Json.encodeToString(serializer, instance)
    if (string != targetString) return "Incorrect serialization result: expected $targetString, got $string"
    konst instance2 = Json.decodeFromString(serializer, string)
    if (instance2.i != instance.i) return "Incorrect deserialization result: expected ${instance.i}, got ${instance2.i}"
    return "OK"
}


