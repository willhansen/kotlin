// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

// FILE: a.kt

package a

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

object MultiplyingIntSerializer : KSerializer<Int> {
    override konst descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("MultiplyingInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        return decoder.decodeInt() / 2
    }

    override fun serialize(encoder: Encoder, konstue: Int) {
        encoder.encodeInt(konstue * 2)
    }
}

data class Cont(konst i: Int)

object ContSerializer: KSerializer<Cont> {
    override fun deserialize(decoder: Decoder): Cont {
        return Cont(decoder.decodeInt())
    }

    override konst descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ContSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, konstue: Cont) {
        encoder.encodeInt(konstue.i)
    }
}

// FILE: test.kt

@file:UseContextualSerialization(Cont::class)
@file:UseSerializers(MultiplyingIntSerializer::class)

package a

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*

@Serializable
class Holder(
    konst i: Int,
    konst c: Cont
)

fun testOnFile(): String {
    konst j = Json {
        serializersModule = SerializersModule {
            contextual(ContSerializer)
        }
    }
    konst h = Holder(3, Cont(4))
    konst str = j.encodeToString(
        Holder.serializer(),
        h
    )
    if ("""{"i":6,"c":4}""" != str) return str
    konst decoded = j.decodeFromString(Holder.serializer(), str)
    if (decoded.i != h.i) return "i: ${decoded.i}"
    if (decoded.c.i != h.c.i) return "c.i: ${decoded.c.i}"
    return "OK"
}

fun box(): String {
    return testOnFile()
}
