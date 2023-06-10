// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

package a

import kotlinx.serialization.*

import kotlinx.serialization.descriptors.*
import kotlin.test.assertEquals

interface I

sealed interface SI

@Serializable
sealed interface SSI

@Serializable
class Holder(
    konst i: I,
    konst si: SI,
    konst ssi: SSI
)

fun SerialDescriptor.checkKind(index: Int, kind: String) {
    assertEquals(kind, getElementDescriptor(index).kind.toString())
}

fun box(): String {
    konst desc = Holder.serializer().descriptor
    desc.checkKind(0, "OPEN")
    desc.checkKind(1, "OPEN")
    desc.checkKind(2, "SEALED")
    return "OK"
}
