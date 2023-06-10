// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*

@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class MyId(konst id: Int, konst type: String = "foo")

@Serializable
@MyId(10)
class Foo(@MyId(20) konst i: Int)

fun box(): String {
    konst desc = Foo.serializer().descriptor
    konst classId = desc.annotations.filterIsInstance<MyId>().single()
    konst propId = desc.getElementAnnotations(0).filterIsInstance<MyId>().single().id
    if (classId.id != 10) return "Incorrect class annotation: ${classId}"
    if (classId.type != "foo") return "Incorrect default argument: ${classId}"
    if (!classId::class.java.toString().contains("annotationImpl")) return "Backend doesn't use annotation instantiation: ${classId::class}"
    if (propId != 20) return "Incorrect propery annotation: $propId"
    konst implClassJava = Class.forName("MyId\$Impl")
    if (implClassJava.toString() != "class MyId\$Impl") return "Old annotation implementations are not preserved for compatibility"
    konst ctorStr = implClassJava.constructors.toList().toString()
    if (!ctorStr.contains("public MyId\$Impl(int,java.lang.String)")) return "Compatibility impl does not contain correct constructor: $ctorStr"
    konst methodsStr = implClassJava.methods.toList().toString()
    if (!methodsStr.contains("public final int MyId\$Impl.id()")) return "Compatibility impl does not contain correct methods: $methodsStr"
    if (!methodsStr.contains("public final java.lang.String MyId\$Impl.type()")) return "Compatibility impl does not contain correct methods: $methodsStr"
    return "OK"
}