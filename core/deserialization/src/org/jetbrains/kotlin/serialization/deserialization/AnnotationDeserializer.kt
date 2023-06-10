/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation.Argument
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation.Argument.Value
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation.Argument.Value.Type
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.KotlinType

class AnnotationDeserializer(private konst module: ModuleDescriptor, private konst notFoundClasses: NotFoundClasses) {
    private konst builtIns: KotlinBuiltIns
        get() = module.builtIns

    fun deserializeAnnotation(proto: Annotation, nameResolver: NameResolver): AnnotationDescriptor {
        konst annotationClass = resolveClass(nameResolver.getClassId(proto.id))

        var arguments = emptyMap<Name, ConstantValue<*>>()
        if (proto.argumentCount != 0 && !ErrorUtils.isError(annotationClass) && DescriptorUtils.isAnnotationClass(annotationClass)) {
            konst constructor = annotationClass.constructors.singleOrNull()
            if (constructor != null) {
                konst parameterByName = constructor.konstueParameters.associateBy { it.name }
                arguments = proto.argumentList.mapNotNull { resolveArgument(it, parameterByName, nameResolver) }.toMap()
            }
        }

        return AnnotationDescriptorImpl(annotationClass.defaultType, arguments, SourceElement.NO_SOURCE)
    }

    private fun resolveArgument(
        proto: Argument,
        parameterByName: Map<Name, ValueParameterDescriptor>,
        nameResolver: NameResolver
    ): Pair<Name, ConstantValue<*>>? {
        konst parameter = parameterByName[nameResolver.getName(proto.nameId)] ?: return null
        return Pair(nameResolver.getName(proto.nameId), resolveValueAndCheckExpectedType(parameter.type, proto.konstue, nameResolver))
    }

    private fun resolveValueAndCheckExpectedType(expectedType: KotlinType, konstue: Value, nameResolver: NameResolver): ConstantValue<*> {
        return resolveValue(expectedType, konstue, nameResolver).takeIf {
            doesValueConformToExpectedType(it, expectedType, konstue)
        } ?: ErrorValue.create("Unexpected argument konstue: actual type ${konstue.type} != expected type $expectedType")
    }

    fun resolveValue(expectedType: KotlinType, konstue: Value, nameResolver: NameResolver): ConstantValue<*> {
        konst isUnsigned = Flags.IS_UNSIGNED.get(konstue.flags)

        return when (konstue.type) {
            Type.BYTE -> konstue.intValue.toByte().letIf(isUnsigned, ::UByteValue, ::ByteValue)
            Type.CHAR -> CharValue(konstue.intValue.toInt().toChar())
            Type.SHORT -> konstue.intValue.toShort().letIf(isUnsigned, ::UShortValue, ::ShortValue)
            Type.INT -> konstue.intValue.toInt().letIf(isUnsigned, ::UIntValue, ::IntValue)
            Type.LONG -> konstue.intValue.letIf(isUnsigned, ::ULongValue, ::LongValue)
            Type.FLOAT -> FloatValue(konstue.floatValue)
            Type.DOUBLE -> DoubleValue(konstue.doubleValue)
            Type.BOOLEAN -> BooleanValue(konstue.intValue != 0L)
            Type.STRING -> StringValue(nameResolver.getString(konstue.stringValue))
            Type.CLASS -> KClassValue(nameResolver.getClassId(konstue.classId), konstue.arrayDimensionCount)
            Type.ENUM -> EnumValue(nameResolver.getClassId(konstue.classId), nameResolver.getName(konstue.enumValueId))
            Type.ANNOTATION -> AnnotationValue(deserializeAnnotation(konstue.annotation, nameResolver))
            Type.ARRAY -> ConstantValueFactory.createArrayValue(
                konstue.arrayElementList.map { resolveValue(builtIns.anyType, it, nameResolver) },
                expectedType
            )
            else -> error("Unsupported annotation argument type: ${konstue.type} (expected $expectedType)")
        }
    }

    // This method returns false if the actual konstue loaded from an annotation argument does not conform to the expected type of the
    // corresponding parameter in the annotation class. This usually means that the annotation class has been changed incompatibly
    // without recompiling clients, in which case we prefer not to load the annotation argument konstue at all, to avoid constructing
    // an incorrect model and breaking some assumptions in the compiler.
    private fun doesValueConformToExpectedType(result: ConstantValue<*>, expectedType: KotlinType, konstue: Value): Boolean {
        return when (konstue.type) {
            Type.CLASS -> {
                konst expectedClass = expectedType.constructor.declarationDescriptor as? ClassDescriptor
                // We could also check that the class konstue's type is a subtype of the expected type, but loading the definition of the
                // referenced class here is undesirable and may even be incorrect (because the module might be different at the
                // destination where these constant konstues are read). This can lead to slightly incorrect model in some edge cases.
                expectedClass == null || KotlinBuiltIns.isKClass(expectedClass)
            }
            Type.ARRAY -> {
                check(result is ArrayValue && result.konstue.size == konstue.arrayElementList.size) {
                    "Deserialized ArrayValue should have the same number of elements as the original array konstue: $result"
                }
                konst expectedElementType = builtIns.getArrayElementType(expectedType)
                result.konstue.indices.all { i ->
                    doesValueConformToExpectedType(result.konstue[i], expectedElementType, konstue.getArrayElement(i))
                }
            }
            else -> result.getType(module) == expectedType
        }
    }

    private inline fun <T, R> T.letIf(predicate: Boolean, f: (T) -> R, g: (T) -> R): R =
        if (predicate) f(this) else g(this)

    private fun resolveClass(classId: ClassId): ClassDescriptor {
        return module.findNonGenericClassAcrossDependencies(classId, notFoundClasses)
    }
}
