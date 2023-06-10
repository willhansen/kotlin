// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

import kotlinx.serialization.*

fun local(): String {
    @Serializable
    data class Carrier(konst i: Int)

    return Carrier.serializer().descriptor.toString()
}

fun box(): String {
    konst expected = "local.Carrier(i: kotlin.Int)"
    konst actual = local()
    if (expected != actual) {
        return "Fail: $actual"
    }
    return "OK"
}
