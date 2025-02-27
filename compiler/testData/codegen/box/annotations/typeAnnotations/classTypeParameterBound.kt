// TARGET_BACKEND: JVM
// EMIT_JVM_TYPE_ANNOTATIONS
// JVM_TARGET: 1.8
// WITH_REFLECT
// FULL_JDK
package foo

import kotlin.reflect.KClass
import kotlin.reflect.full.functions

@Target(AnnotationTarget.TYPE)
annotation class TypeAnn(konst name: String)

interface SimpleInterface
open class SimpleClass

class InterfaceClassBound<T>()  where T : @TypeAnn("Interface") SimpleInterface, T : @TypeAnn("Class") SimpleClass {

}

class ClassInterfaceBound<T>() where T : @TypeAnn("Class") SimpleClass, T : @TypeAnn("Interface") SimpleInterface  {

}

fun box() : String {
    konst interfaceBounds = InterfaceClassBound::class.typeParameters.single()
    if (interfaceBounds.upperBounds[0].annotations.joinToString() != "@foo.TypeAnn(name=Interface)") return "fail 1: ${interfaceBounds.upperBounds[0].annotations.joinToString()}"
    if ((interfaceBounds.upperBounds[0].classifier as KClass<*>).simpleName != "SimpleInterface") return "fail 1.1: ${interfaceBounds.upperBounds[0].classifier}"

    if (interfaceBounds.upperBounds[1].annotations.joinToString() != "@foo.TypeAnn(name=Class)") return "fail 2: ${interfaceBounds.upperBounds[1].annotations.joinToString()}"
    if ((interfaceBounds.upperBounds[1].classifier as KClass<*>).simpleName != "SimpleClass") return "fail 2.1: ${interfaceBounds.upperBounds[1].classifier}"

    konst classBounds = ClassInterfaceBound::class.typeParameters.single()
    if (classBounds.upperBounds[0].annotations.joinToString() != "@foo.TypeAnn(name=Class)") return "fail 3: ${classBounds.upperBounds[0].annotations.joinToString()}"
    if ((classBounds.upperBounds[0].classifier as KClass<*>).simpleName != "SimpleClass") return "fail 3.1: ${classBounds.upperBounds[0].classifier}"

    if (classBounds.upperBounds[1].annotations.joinToString() != "@foo.TypeAnn(name=Interface)") return "fail 4: ${classBounds.upperBounds[1].annotations.joinToString()}"
    if ((classBounds.upperBounds[1].classifier as KClass<*>).simpleName != "SimpleInterface") return "fail 4.1: ${classBounds.upperBounds[1].classifier}"

    return "OK"
}

