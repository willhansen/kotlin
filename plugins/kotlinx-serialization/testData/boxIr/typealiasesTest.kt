// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.util.Date

class NotSerializable(konst i: Int)

object SerializerB : KSerializer<NotSerializable> {
    override konst descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("SerializerB", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: NotSerializable) = TODO()

    override fun deserialize(decoder: Decoder): NotSerializable = TODO()
}

object DateSer : KSerializer<Date> {
    override konst descriptor = PrimitiveSerialDescriptor("DateSer", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Date = TODO()

    override fun serialize(encoder: Encoder, konstue: Date) = TODO()
}

typealias S = @Serializable(SerializerB::class) NotSerializable
typealias BS = @Serializable(DateSer::class) Date

@Serializable
class Foo(
    konst s: S,
    konst bs: BS,
    konst list: List<BS>
)

fun box(): String {
    konst list = Foo.serializer().descriptor.elementDescriptors.map { it.serialName }.toList()
    if (list != listOf("SerializerB", "DateSer", "kotlin.collections.ArrayList")) return list.toString()
    return "OK"
}