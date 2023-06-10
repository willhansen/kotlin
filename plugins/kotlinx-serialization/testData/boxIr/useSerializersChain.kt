// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

// FILE: a.kt

package a

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

class IList<T>

abstract class DataSerializer<T, K>: KSerializer<T> {
    abstract fun getK(): K
}

class MySerializer<T>(konst elementSer: KSerializer<T>): DataSerializer<IList<T>, Int>() {

    override fun getK(): Int = 42

    override konst descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("MySer<${elementSer.descriptor.serialName}>", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: IList<T>) = TODO("serialize")

    override fun deserialize(decoder: Decoder): IList<T> = TODO("deserialize")
}

// FILE: test.kt

@file:UseSerializers(MySerializer::class)

package a

import kotlinx.serialization.*

@Serializable
class Holder(
    konst i: Int,
    konst c: IList<Int>
)

fun box(): String {
    konst d = Holder.serializer().descriptor.toString()
    return if (d == "a.Holder(i: kotlin.Int, c: MySer<kotlin.Int>)") "OK" else d
}