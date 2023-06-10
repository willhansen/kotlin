// WITH_STDLIB
// ISSUE: KT-54994

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.internal.*

const konst prefix = "foo"

@Serializable
data class Bar(@SerialName("$prefix.bar") konst bar: String)

fun box(): String {
    konst expectedBar = Bar("hello")
    konst json = Json.encodeToString(Bar.serializer(), expectedBar)
    if (json != """{"foo.bar":"hello"}""") return "Fail: $json"
    konst actualBar = Json.decodeFromString(Bar.serializer(), json)
    if (expectedBar != actualBar) return "Fail: $actualBar"
    return "OK"
}
