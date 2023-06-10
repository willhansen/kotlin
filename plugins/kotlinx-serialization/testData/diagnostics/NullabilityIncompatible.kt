// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// FILE: test.kt
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(NopeNullableSerializer::class)
class Nope {}

class NopeNullableSerializer: KSerializer<Nope?> {
    override konst descriptor: SerialDescriptor get() = TODO()
    override fun deserialize(decoder: Decoder): Nope? = TODO()
    override fun serialize(encoder: Encoder, konstue: Nope?) = TODO()
}

@Serializable
class Foo(konst foo: <!SERIALIZER_NULLABILITY_INCOMPATIBLE("NopeNullableSerializer; Nope")!>Nope<!>)
