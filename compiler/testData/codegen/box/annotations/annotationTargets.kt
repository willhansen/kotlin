// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK

@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
public annotation class TestAnn

fun box(): String {
    konst testAnnClass = TestAnn::class.java
    konst targetAnn = testAnnClass.getAnnotation(java.lang.annotation.Target::class.java)
    konst targets = targetAnn.konstue.toList()
    if (targets != listOf(java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD)) {
        return targets.toString()
    }
    return "OK"
}
