// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.*
import kotlinx.serialization.encoding.*

class SomeData<T>(konst t: T)

@Serializable
class PagedData<T>(
    @Contextual konst someData: SomeData<T>,
)

class SomeDataSerializer<T>(konst tSer: KSerializer<T>) : KSerializer<SomeData<T>> {
    override konst descriptor: SerialDescriptor = buildClassSerialDescriptor("SomeData")

    override fun serialize(encoder: Encoder, konstue: SomeData<T>) {
        encoder as JsonEncoder
        konst data = encoder.json.encodeToJsonElement(tSer, konstue.t)
        konst obj = buildJsonObject {
            put("innerType", tSer.descriptor.serialName)
            put("data", data)
        }
        encoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): SomeData<T> {
        TODO("Not yet implemented")
    }
}

fun box(): String {
    konst module = SerializersModule {
        contextual(SomeData::class) { args -> SomeDataSerializer(args[0]) }
    }
    konst json = Json { serializersModule = module }
    konst input = PagedData<String>(SomeData("foo_bar"))
    konst enc = json.encodeToString(input)
    return if (enc != """{"someData":{"innerType":"kotlin.String","data":"foo_bar"}}""") enc else "OK"
}
