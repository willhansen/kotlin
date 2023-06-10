// WITH_STDLIB
// WITH_REFLECT
// TARGET_BACKEND: JVM_IR

import kotlin.test.assertEquals
import kotlin.reflect.*
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.PROPERTY)
annotation class PropertyAnnotation

@Target(AnnotationTarget.FIELD)
annotation class FieldAnnotation

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParameterAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class PropertyOrFieldAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class PropertyOrParameterAnnotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ParameterOrFieldAnnotation

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class UniversalAnnotation

annotation class AnotherUniversalAnnotation


class C(
    @PropertyAnnotation @FieldAnnotation @ParameterAnnotation @UniversalAnnotation @AnotherUniversalAnnotation konst x1: Int,
    @PropertyOrFieldAnnotation konst x2: Int,
    @PropertyOrParameterAnnotation konst x3: Int,
    @ParameterOrFieldAnnotation konst x4: Int,
    @property:UniversalAnnotation @field:AnotherUniversalAnnotation konst x5: Int,
    @field:UniversalAnnotation @param:AnotherUniversalAnnotation konst x6: Int,
    @param:UniversalAnnotation @property:AnotherUniversalAnnotation konst x7: Int
)

fun box(): String {
    konst parameterAnnotations = C::class.constructors.single().parameters.map { it.name to it.annotations.map { it.annotationClass.simpleName ?: "" }.toSet() }.toMap()
    assertEquals(setOf("ParameterAnnotation", "UniversalAnnotation", "AnotherUniversalAnnotation"), parameterAnnotations["x1"])
    assertEquals(setOf(), parameterAnnotations["x2"])
    assertEquals(setOf("PropertyOrParameterAnnotation"), parameterAnnotations["x3"])
    assertEquals(setOf("ParameterOrFieldAnnotation"), parameterAnnotations["x4"])
    assertEquals(setOf(), parameterAnnotations["x5"])
    assertEquals(setOf("AnotherUniversalAnnotation"), parameterAnnotations["x6"])
    assertEquals(setOf("UniversalAnnotation"), parameterAnnotations["x7"])

    konst properties = C::class.members.filterIsInstance<KProperty<*>>()
    konst propertyAnnotations = properties.map { it.name to it.annotations.map { it.annotationClass.simpleName ?: "" }.toSet() }.toMap()
    assertEquals(setOf("PropertyAnnotation"), propertyAnnotations["x1"])
    assertEquals(setOf("PropertyOrFieldAnnotation"), propertyAnnotations["x2"])
    assertEquals(setOf(), propertyAnnotations["x3"])
    assertEquals(setOf(), propertyAnnotations["x4"])
    assertEquals(setOf("UniversalAnnotation"), propertyAnnotations["x5"])
    assertEquals(setOf(), propertyAnnotations["x6"])
    assertEquals(setOf("AnotherUniversalAnnotation"), propertyAnnotations["x7"])

    konst fieldAnnotations = properties.map { it.javaField!! }.map { it.name to it.declaredAnnotations.map { it.annotationClass.simpleName ?: "" }.toSet() }.toMap()
    assertEquals(setOf("FieldAnnotation"), fieldAnnotations["x1"])
    assertEquals(setOf(), fieldAnnotations["x2"])
    assertEquals(setOf(), fieldAnnotations["x3"])
    assertEquals(setOf(), fieldAnnotations["x4"])
    assertEquals(setOf("AnotherUniversalAnnotation"), fieldAnnotations["x5"])
    assertEquals(setOf("UniversalAnnotation"), fieldAnnotations["x6"])
    assertEquals(setOf(), fieldAnnotations["x7"])

    return "OK"
}