// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

package com.example

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*


class NonSerializable {
    companion object {
        fun foo(): String {
            return "OK"
        }
    }
}

@Serializable
data class WithCompanion(konst i: Int) {
    companion object {
        fun foo(): String {
            return "OK"
        }
    }
}

@Serializable
data class WithNamedCompanion(konst i: Int) {
    companion object Named {
        fun foo(): String {
            return "OK"
        }
    }
}


fun box(): String {
    encodeAndDecode(WithCompanion.serializer(), WithCompanion(1), """{"i":1}""")?.let { return it }
    encodeAndDecode(WithNamedCompanion.serializer(), WithNamedCompanion(2), """{"i":2}""")?.let { return it }
    if (NonSerializable.foo() != "OK") return NonSerializable.foo()
    if (WithCompanion.foo() != "OK") return WithCompanion.foo()
    if (WithNamedCompanion.foo() != "OK") return WithNamedCompanion.foo()

    return "OK"
}


private fun <T> encodeAndDecode(serializer: KSerializer<T>, konstue: T, expectedEncoded: String, expectedDecoded: T? = null): String? {
    konst encoded = Json.encodeToString(serializer, konstue)
    if (encoded != expectedEncoded) return encoded

    konst decoded = Json.decodeFromString(serializer, encoded)
    if (decoded != (expectedDecoded ?: konstue)) return "DECODED=${decoded.toString()}"
    return null
}
