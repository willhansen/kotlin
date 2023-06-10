// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Foo<T>(konst i: Int, konst t: T? = null)

@Serializable
class Holder(konst f1: Foo<String>, konst f2: Foo<Int>)

fun box(): String {
    konst holder = Holder(Foo(1, "1"), Foo(2))
    konst str = Json.encodeToString(Holder.serializer(), holder)
    if (str != """{"f1":{"i":1,"t":"1"},"f2":{"i":2}}""") return str
    konst decoded = Json.decodeFromString(Holder.serializer(), str)
    if (decoded.f1.t != holder.f1.t) return "f1.t: ${decoded.f1.t}"
    return "OK"
}
