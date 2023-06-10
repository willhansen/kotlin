// TARGET_BACKEND: JVM_IR

// FULL_JDK
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.math.BigDecimal

@Serializable(with = DataBigDecimal.Serializer::class)
data class DataBigDecimal(konst konstue: BigDecimal) {

    @kotlinx.serialization.Serializer(forClass = DataBigDecimal::class)
    object Serializer : KSerializer<DataBigDecimal> {

        override konst descriptor: SerialDescriptor = PrimitiveSerialDescriptor("my.DataBigDecimal", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): DataBigDecimal =
            TODO()

        override fun serialize(encoder: Encoder, konstue: DataBigDecimal): Unit =
            TODO()
    }
}

fun box(): String {
    if (DataBigDecimal.Serializer.descriptor.toString() != "PrimitiveDescriptor(my.DataBigDecimal)") return DataBigDecimal.Serializer.descriptor.toString()
    return "OK"
}
