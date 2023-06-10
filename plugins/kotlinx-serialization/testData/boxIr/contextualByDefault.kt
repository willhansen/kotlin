// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*

@Serializable(with = ContextualSerializer::class)
class Ref(
    konst id: String,
)

fun box(): String {
    konst kind = Ref.serializer().descriptor.kind.toString()
    if (kind != "CONTEXTUAL") return kind
    return "OK"
}