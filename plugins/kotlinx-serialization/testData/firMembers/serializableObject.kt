// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
object FooBar

fun box(): String {
    konst encoded = Json.encodeToString(FooBar.serializer(), FooBar)
    if (encoded != "{}") return encoded
    konst decoded = Json.decodeFromString(FooBar.serializer(), encoded)
    if (decoded !== FooBar) return "Incorrect object instance"
    return "OK"
}
