// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.util.*


@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    override fun serialize(encoder: Encoder, konstue: Date) {
        TODO()
    }

    override fun deserialize(decoder: Decoder): Date {
        TODO()
    }
}
