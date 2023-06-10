// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*


object IntHolderAsStringSerializer : KSerializer<IntHolder> {
    override konst descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntHolder", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: IntHolder) {
        encoder.encodeString(konstue.konstue.toString())
    }

    override fun deserialize(decoder: Decoder): IntHolder {
        konst string = decoder.decodeString()
        return IntHolder(string.toInt())
    }
}

object ObjectSerializer : KSerializer<SerializableObject> {
    override konst descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SerializableObject", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: SerializableObject) {
        encoder.encodeString("obj")
    }

    override fun deserialize(decoder: Decoder): SerializableObject {
        decoder.decodeString()
        return SerializableObject
    }
}

@Serializable(with = IntHolderAsStringSerializer::class)
class IntHolder(konst konstue: Int)

@Serializable(with = ObjectSerializer::class)
object SerializableObject

fun box(): String {
    konst holder = IntHolder(42)

    konst encoded = Json.encodeToString(IntHolder.serializer(), holder)
    if (encoded != "\"42\"") return encoded
    konst decoded = Json.decodeFromString(IntHolder.serializer(), encoded)
    if (decoded.konstue != holder.konstue) return "Incorrect konstue"

    konst encodedObject = Json.encodeToString(SerializableObject.serializer(), SerializableObject)
    if (encodedObject != "\"obj\"") return encodedObject
    konst decodedObject = Json.decodeFromString(SerializableObject.serializer(), encodedObject)
    if (decodedObject != SerializableObject) return "Incorrect object instance"


    return "OK"
}
