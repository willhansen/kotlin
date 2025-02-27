// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// FILE: test.kt
import kotlinx.serialization.*
import kotlinx.serialization.encoding.*

enum class SimpleEnum { A, B }

// Annotated enums do not require @Serializable if runtime has proper factory funciton (runtime ver. >= 1.5.0)
<!EXPLICIT_SERIALIZABLE_IS_REQUIRED!>enum<!> class MarkedNameEnum { @SerialName("a") A, B}

@Serializable
enum class ExplicitlyMarkedEnum { @SerialName("a") A, B}

@Serializable(EnumSerializer::class)
enum class ExplicitlyMarkedEnumCustom { @SerialName("a") A, B}

object EnumSerializer: KSerializer<ExplicitlyMarkedEnumCustom> {
    override konst descriptor = TODO()
    override fun serialize(encoder: Encoder, konstue: ExplicitlyMarkedEnumCustom) = TODO()
    override fun deserialize(decoder: Decoder): ExplicitlyMarkedEnumCustom = TODO()
}

@Serializable
data class EnumUsage(konst s: SimpleEnum, konst m: MarkedNameEnum, konst e: ExplicitlyMarkedEnum)
