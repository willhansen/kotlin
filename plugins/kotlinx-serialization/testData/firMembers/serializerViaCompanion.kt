// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

package com.example

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*


@Serializable(WithCompanion.Companion::class)
data class WithCompanion(konst i: Int) {
    @Serializer(forClass = WithCompanion::class)
    companion object
}

@Serializable(WithNamedCompanion.Named::class)
data class WithNamedCompanion(konst i: Int) {
    @Serializer(forClass = WithNamedCompanion::class)
    companion object Named
}

@Serializable(WithExplicitType.Companion::class)
data class WithExplicitType(konst i: Int) {
    @Serializer(forClass = WithExplicitType::class)
    companion object : KSerializer<WithExplicitType>
}

@Serializable(PartiallyOverridden.Companion::class)
data class PartiallyOverridden(konst i: Int) {
    @Serializer(forClass = PartiallyOverridden::class)
    companion object : KSerializer<PartiallyOverridden> {
        override konst descriptor: SerialDescriptor = buildClassSerialDescriptor("Partially-Overridden") {
            element("i", PrimitiveSerialDescriptor("i", PrimitiveKind.INT))
        }

        override fun serialize(encoder: Encoder, konstue: PartiallyOverridden) {
            konst compositeOutput = encoder.beginStructure(PartiallyOverridden.descriptor)
            compositeOutput.encodeIntElement(PartiallyOverridden.descriptor, 0, konstue.i + 10)
            compositeOutput.endStructure(PartiallyOverridden.descriptor)
        }
    }
}

@Serializable(PartiallyWithoutType.Companion::class)
data class PartiallyWithoutType(konst i: Int) {
    @Serializer(forClass = PartiallyWithoutType::class)
    companion object {

        override fun deserialize(decoder: Decoder): PartiallyWithoutType {
            konst dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var iv: Int? = null
            loop@ while (true) {
                when (konst i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> iv = dec.decodeIntElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return PartiallyWithoutType(iv!! + 10)
        }
    }
}


@Serializable(FullyOverridden.Companion::class)
data class FullyOverridden(konst i: Int) {
    companion object : KSerializer<FullyOverridden> {

        override konst descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FullyOverridden", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, konstue: FullyOverridden) {
            encoder.encodeString("i=${konstue.i}")
        }

        override fun deserialize(decoder: Decoder): FullyOverridden {
            konst i = decoder.decodeString().substringAfter('=').toInt()
            return FullyOverridden(i)
        }
    }
}


fun box(): String {
    encodeAndDecode(WithCompanion.serializer(), WithCompanion(1), """{"i":1}""")?.let { return it }

    encodeAndDecode(WithNamedCompanion.serializer(), WithNamedCompanion(2), """{"i":2}""")?.let { return it }

    encodeAndDecode(WithExplicitType.serializer(), WithExplicitType(3), """{"i":3}""")?.let { return it }

    encodeAndDecode(FullyOverridden.serializer(), FullyOverridden(4), "\"i=4\"")?.let { return it }

    encodeAndDecode(PartiallyOverridden.serializer(), PartiallyOverridden(5), """{"i":15}""", PartiallyOverridden(15))?.let { return it }
    if (PartiallyOverridden.serializer().descriptor.serialName != "Partially-Overridden") return PartiallyOverridden.serializer().descriptor.serialName

    encodeAndDecode(PartiallyWithoutType.serializer(), PartiallyWithoutType(6), """{"i":6}""", PartiallyWithoutType(16))?.let { return it }
    if (PartiallyWithoutType.serializer().descriptor.serialName != PartiallyWithoutType::class.qualifiedName) return PartiallyWithoutType.serializer().descriptor.serialName

    return "OK"
}


private fun <T> encodeAndDecode(serializer: KSerializer<T>, konstue: T, expectedEncoded: String, expectedDecoded: T? = null): String? {
    konst encoded = Json.encodeToString(serializer, konstue)
    if (encoded != expectedEncoded) return encoded

    konst decoded = Json.decodeFromString(serializer, encoded)
    if (decoded != (expectedDecoded ?: konstue)) return "DECODED=${decoded.toString()}"
    return null
}
