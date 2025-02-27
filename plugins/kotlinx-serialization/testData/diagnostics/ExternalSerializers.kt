// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// SKIP_TXT

// FILE: test.kt
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

class Foo(i: Int, konst j: Int)

<!EXTERNAL_CLASS_NOT_SERIALIZABLE!>@Serializer(forClass = Foo::class)<!>
object ExternalSerializer

<!EXTERNAL_SERIALIZER_USELESS!>@Serializer(forClass = Foo::class)<!>
object UselessExternalSerializer : KSerializer<Foo> {
    override konst descriptor: SerialDescriptor
        get() {
            TODO()
        }

    override fun serialize(encoder: Encoder, konstue: Foo) {
        TODO()
    }

    override fun deserialize(decoder: Decoder): Foo {
        TODO()
    }
}
