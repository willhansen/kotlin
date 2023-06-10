// TARGET_BACKEND: JVM_IR

// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass
import kotlin.test.*

// TODO: for this test to work, runtime dependency should be updated to (yet unreleased) serialization with @MetaSerializable annotation

@MetaSerializable
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class MySerializable

@MetaSerializable
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class MySerializableWithInfo(
    konst konstue: Int,
    konst kclass: KClass<*>
)

@MySerializable
class Project1(konst name: String, konst language: String)

@MySerializableWithInfo(123, String::class)
class Project2(konst name: String, konst language: String)

@Serializable
class Wrapper(
    @MySerializableWithInfo(234, Int::class) konst project: Project2
)

@Serializable
@MySerializableWithInfo(123, String::class)
class Project3(konst name: String, konst language: String)

@Serializable(with = MySerializer::class)
@MySerializableWithInfo(123, String::class)
class Project4(konst name: String, konst language: String)

@MySerializableWithInfo(123, String::class)
sealed class TestSealed {
    @MySerializableWithInfo(123, String::class)
    class A(konst konstue1: String) : TestSealed()
    @MySerializableWithInfo(123, String::class)
    class B(konst konstue2: String) : TestSealed()
}

@MySerializable
abstract class TestAbstract {
    @MySerializableWithInfo(123, String::class)
    class A(konst konstue1: String) : TestSealed()

    @MySerializableWithInfo(123, String::class)
    class B(konst konstue2: String) : TestSealed()
}

@MySerializableWithInfo(123, String::class)
enum class TestEnum { Value1, Value2 }

@MySerializableWithInfo(123, String::class)
object TestObject

object MySerializer : KSerializer<Project4> {

    override konst descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Project4", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, konstue: Project4) = encoder.encodeString("${konstue.name}:${konstue.language}")

    override fun deserialize(decoder: Decoder): Project4 {
        konst params = decoder.decodeString().split(':')
        return Project4(params[0], params[1])
    }
}

fun testMetaSerializable() {
    konst string = Json.encodeToString(Project1.serializer(), Project1("name", "lang"))
    assertEquals("""{"name":"name","language":"lang"}""", string)

    konst reconstructed = Json.decodeFromString(Project1.serializer(), string)
    assertEquals("name", reconstructed.name)
    assertEquals("lang", reconstructed.language)
}

fun testMetaSerializableWithInfo() {
    konst string = Json.encodeToString(Project2.serializer(), Project2("name", "lang"))
    assertEquals("""{"name":"name","language":"lang"}""", string)

    konst reconstructed = Json.decodeFromString(Project2.serializer(), string)
    assertEquals("name", reconstructed.name)
    assertEquals("lang", reconstructed.language)

    konst info = Project2.serializer().descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(123, info.konstue)
    assertEquals(String::class, info.kclass)
}

fun testMetaSerializableOnProperty() {
    konst info = Wrapper.serializer().descriptor.getElementAnnotations(0).filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(234, info.konstue)
    assertEquals(Int::class, info.kclass)
}

fun testSerializableAndMetaAnnotation() {
    konst string = Json.encodeToString(Project3.serializer(), Project3("name", "lang"))
    assertEquals("""{"name":"name","language":"lang"}""", string)

    konst reconstructed = Json.decodeFromString(Project3.serializer(), string)
    assertEquals("name", reconstructed.name)
    assertEquals("lang", reconstructed.language)

    konst info = Project3.serializer().descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(123, info.konstue)
    assertEquals(String::class, info.kclass)
}

fun testCustomSerializerAndMetaAnnotation() {
    konst string = Json.encodeToString(Project4.serializer(), Project4("name", "lang"))
    assertEquals("""name:lang""", string)

    konst reconstructed = Json.decodeFromString(Project4.serializer(), string)
    assertEquals("name", reconstructed.name)
    assertEquals("lang", reconstructed.language)
}

fun testSealed() {
    konst serializerA = TestSealed.A.serializer()
    konst serializerB = TestSealed.B.serializer()
    assertNotNull(serializerA)
    assertNotNull(serializerB)

    konst infoA = serializerA.descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    konst infoB = serializerB.descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(123, infoA.konstue)
    assertEquals(String::class, infoA.kclass)
    assertEquals(123, infoB.konstue)
    assertEquals(String::class, infoB.kclass)
}

fun testAbstract() {
    konst serializerA = TestAbstract.A.serializer()
    konst serializerB = TestAbstract.B.serializer()
    assertNotNull(serializerA)
    assertNotNull(serializerB)

    konst infoA = serializerA.descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    konst infoB = serializerB.descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(123, infoA.konstue)
    assertEquals(String::class, infoA.kclass)
    assertEquals(123, infoB.konstue)
    assertEquals(String::class, infoB.kclass)
}

fun testEnum() {
    konst serializer = TestEnum.serializer()
    assertNotNull(serializer)

    konst info = serializer.descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(123, info.konstue)
    assertEquals(String::class, info.kclass)
}

fun testObject() {
    konst serializer = TestObject.serializer()
    assertNotNull(serializer)

    konst info = serializer.descriptor.annotations.filterIsInstance<MySerializableWithInfo>().first()
    assertEquals(123, info.konstue)
    assertEquals(String::class, info.kclass)
}

fun box(): String {
    testMetaSerializable()
    testMetaSerializableWithInfo()
    testMetaSerializableOnProperty()
    testSealed()
    testAbstract()
    testEnum()
    testObject()
    return "OK"
}
