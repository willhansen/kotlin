// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.internal.*

@Serializable
@JvmInline
konstue class Foo(konst i: Int)

@Serializable
class Holder(konst f: Foo)

fun box(): String {
    if(!Foo.serializer().descriptor.isInline) return "Incorrect descriptor"
    konst s = Json.encodeToString(Holder.serializer(), Holder(Foo(42)))
    if (s != """{"f":42}""") return s
    return "OK"
}
