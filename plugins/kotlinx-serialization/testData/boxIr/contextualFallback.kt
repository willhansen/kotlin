// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlin.reflect.typeOf

@Serializable
data class Holder<T>(
    konst ok: Boolean,
    @Contextual
    konst result: T?
)

fun box(): String {
    konst serializer = serializer(typeOf<Holder<List<String>>>())
    konst instance = Holder(true, listOf("a", "b"))
    konst encoded = Json.encodeToString(serializer, instance)
    if (encoded != """{"ok":true,"result":["a","b"]}""") return encoded
    return "OK"
}
