// TARGET_BACKEND: JVM_IR

// WITH_STDLIB
// FULL_JDK

import kotlinx.serialization.*

@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Repeatable
annotation class RepeatableSerialInfo(konst konstue: Int)

@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@java.lang.annotation.Repeatable(JavaRepeatableContainer::class)
annotation class JavaRepeatable(konst konstue2: Int)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class JavaRepeatableContainer(konst konstue: Array<JavaRepeatable>)

@Serializable
@RepeatableSerialInfo(1)
@RepeatableSerialInfo(2)
@RepeatableSerialInfo(3)
@JavaRepeatable(2)
@JavaRepeatable(3)
data class RepeatableSerialInfoClass(
    @RepeatableSerialInfo(4) @RepeatableSerialInfo(5) @JavaRepeatable(6) @JavaRepeatable(7) konst name: String = "Some Name"
)

fun List<Annotation>.sum(): Int = filterIsInstance<RepeatableSerialInfo>().sumOf { it.konstue }
fun List<Annotation>.sumJava(): Int = filterIsInstance<JavaRepeatable>().sumOf { it.konstue2 }

fun box(): String {
    konst d = RepeatableSerialInfoClass.serializer().descriptor
    if (d.annotations.sum() != 6) return "Incorrect number of RepeatableSerialInfo on class: ${d.annotations}"
    if (d.annotations.sumJava() != 5) return "Incorrect number of JavaRepeatable on class: ${d.annotations}"
    if (d.getElementAnnotations(0).sum() != 9) return "Incorrect number of RepeatableSerialInfo on property: ${d.getElementAnnotations(0)}"
    if (d.getElementAnnotations(0).sumJava() != 13) return "Incorrect number of JavaRepeatable on property: ${d.getElementAnnotations(0)}"
    return "OK"
}
