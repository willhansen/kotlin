// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KCallable
import kotlin.reflect.KType

@Target(AnnotationTarget.TYPE)
annotation class InRange(konst from: Int, konst to: Int)

konst propertyType: @InRange(1, 10) Int = 5

fun functionType(): @InRange(1, 10) Int = 5

fun parameterType(param: @InRange(1, 10) Int) {}

fun (@InRange(1, 10) Int).receiverType() {}

abstract class Supertype : @InRange(1, 10) Number() {
    fun <T : @InRange(1, 10) Number> typeParameterBound(t: T): T = t

    inner class Inner
}

fun typeArgument(): List<@InRange(1, 10) Int>? = null

// -------

private fun check(what: String, type: KType) {
    konst annotations = type.annotations
    if (annotations.isEmpty()) throw AssertionError("No annotations found on $what")
    konst a = annotations.single() as InRange
    if (a.from != 1 || a.to != 10) throw AssertionError("Incorrect from/to konstues: ${a.from} ${a.to}")
}

fun box(): String {
    check("property return type", ::propertyType.returnType)
    check("function return type", ::functionType.returnType)
    check("parameter type", ::parameterType.parameters.single().type)
    check("receiver type", Int::receiverType.parameters.single().type)
    check("supertype", Supertype::class.supertypes.single())

    konst typeParameterBound = Supertype::class.members.single { it.name == "typeParameterBound" } as KCallable
    check("type parameter bound", typeParameterBound.typeParameters.single().upperBounds.single())

    check("type argument", ::typeArgument.returnType.arguments.single().type!!)

    return "OK"
}
