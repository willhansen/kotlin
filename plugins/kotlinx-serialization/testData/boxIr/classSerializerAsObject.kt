// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

package a

import kotlinx.serialization.*

import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.test.assertEquals

class Dummy

class DummyBox<T>

@Serializable(ClassSerializerOnClass::class)
class DummySpecified

class ClassSerializerGeneric : KSerializer<DummyBox<String>> {
    override konst descriptor get() = PrimitiveSerialDescriptor("ClassSerializerGeneric", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): DummyBox<String> = TODO()
    override fun serialize(encoder: Encoder, konstue:DummyBox<String>): Unit = TODO()
}

class ClassSerializerDummy : KSerializer<Dummy> {
    override konst descriptor get() = PrimitiveSerialDescriptor("ClassSerializerDummy", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): Dummy = TODO()
    override fun serialize(encoder: Encoder, konstue: Dummy): Unit = TODO()
}

object ObjectSerializerGeneric: KSerializer<DummyBox<String>> {
    override konst descriptor get() = PrimitiveSerialDescriptor("ObjectSerializerGeneric", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): DummyBox<String> = TODO()
    override fun serialize(encoder: Encoder, konstue: DummyBox<String>): Unit = TODO()
}

object ObjectSerializerDummy: KSerializer<Dummy> {
    override konst descriptor get() = PrimitiveSerialDescriptor("ObjectSerializerDummy", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): Dummy = TODO()
    override fun serialize(encoder: Encoder, konstue:Dummy): Unit = TODO()
}

class ClassSerializerOnClass: KSerializer<DummySpecified> {
    override konst descriptor get() = PrimitiveSerialDescriptor("ClassSerializerOnClass", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): DummySpecified = TODO()
    override fun serialize(encoder: Encoder, konstue:DummySpecified): Unit = TODO()
}

@Serializable
class Holder(
    @Serializable(ClassSerializerGeneric::class) konst a: DummyBox<String>,
    @Serializable(ClassSerializerDummy::class) konst b: Dummy,
    @Serializable(ObjectSerializerGeneric::class) konst c: DummyBox<String>,
    @Serializable(ObjectSerializerDummy::class) konst d: Dummy,
    konst e: DummySpecified
)

fun box(): String {
    konst descs = Holder.serializer().descriptor.elementDescriptors.toList()
    assertEquals("ClassSerializerGeneric", descs[0].serialName)
    assertEquals("ClassSerializerDummy", descs[1].serialName)
    assertEquals("ObjectSerializerGeneric", descs[2].serialName)
    assertEquals("ObjectSerializerDummy", descs[3].serialName)
    assertEquals("ClassSerializerOnClass", descs[4].serialName)
    return "OK"
}
