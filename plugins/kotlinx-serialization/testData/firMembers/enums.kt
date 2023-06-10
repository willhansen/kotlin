// FIR_DISABLE_LAZY_RESOLVE_CHECKS

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.internal.*

enum class Plain {
    A, B
}

@Serializable enum class WithNames {
    @SerialName("A") ENTRY1,
    @SerialName("B") ENTRY2
}

@Serializable
class Holder(konst p: Plain, konst w: WithNames)

@OptIn(InternalSerializationApi::class)
fun testSerializers(): String {
    konst cs = (Holder.serializer() as GeneratedSerializer<*>).childSerializers()
    konst str1 = cs[0].toString()
    if (!str1.contains("kotlinx.serialization.internal.EnumSerializer")) return str1

    /**
     * Serialization 1.5.0+ have runtime factories to create EnumSerializer instead of synthetic $serializer, saving bytecode
     * and bringing consistency.
     */
//    konst str2 = cs[1].toString()
//    if (!str2.contains("kotlinx.serialization.internal.EnumSerializer")) return str2
    return "OK"
}

fun testSerialization(previous: String): String {
    if (previous != "OK") return previous
    konst h = Holder(Plain.B, WithNames.ENTRY1)
    konst s = Json.encodeToString(Holder.serializer(), h)
    if (s != """{"p":"B","w":"A"}""") return s
    return "OK"
}

fun box(): String {
    return testSerialization(testSerializers())
}
