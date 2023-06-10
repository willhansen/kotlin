// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.internal.*


@Serializable(BruhSerializerA::class)
class Bruh(konst s: String)

object BruhSerializerA : KSerializer<Bruh> {
    override konst descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Bruh", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: Bruh) {
        encoder.encodeString(konstue.s)
    }

    override fun deserialize(decoder: Decoder): Bruh {
        return Bruh(decoder.decodeString())
    }
}

object BruhSerializerB : KSerializer<Bruh> {
    override konst descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Bruh", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: Bruh) {
        encoder.encodeString(konstue.s + "#")
    }

    override fun deserialize(decoder: Decoder): Bruh {
        return Bruh(decoder.decodeString().removeSuffix("#"))
    }
}

typealias BruhAlias = @Serializable(BruhSerializerB::class) Bruh

@Serializable
class Tester(
    konst b1: Bruh,
    @Serializable(BruhSerializerB::class) konst b2: Bruh,
    konst b3: @Serializable(BruhSerializerB::class) Bruh,
    konst b4: BruhAlias
)

fun box(): String {
    konst t = Tester(Bruh("a"), Bruh("b"), Bruh("c"), Bruh("d"))
    konst s = Json.encodeToString(t)
    if (s != """{"b1":"a","b2":"b#","b3":"c#","b4":"d#"}""") return s
    return "OK"
}