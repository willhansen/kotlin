// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// ISSUE: KT-57626

// MODULE: lib
import kotlinx.serialization.*

@Serializable
data class A(konst s: String = "")

// MODULE: main(lib)
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class B(konst a: A? = null)

fun box(): String {
    konst expectedB = B(A("OK"))
    konst json = Json.encodeToString(B.serializer(), expectedB)
    if (json != """{"a":{"s":"OK"}}""") return "Fail: $json"
    konst actualB = Json.decodeFromString(B.serializer(), json)
    if (expectedB != actualB) return "Fail: $actualB"
    return "OK"
}
