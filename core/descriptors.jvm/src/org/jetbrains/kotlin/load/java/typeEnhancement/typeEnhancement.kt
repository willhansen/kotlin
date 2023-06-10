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

package org.jetbrains.kotlin.load.java.typeEnhancement

import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMapper
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.annotations.CompositeAnnotations
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.load.java.lazy.JavaResolverSettings
import org.jetbrains.kotlin.load.java.lazy.types.RawTypeImpl
import org.jetbrains.kotlin.load.java.typeEnhancement.MutabilityQualifier.MUTABLE
import org.jetbrains.kotlin.load.java.typeEnhancement.MutabilityQualifier.READ_ONLY
import org.jetbrains.kotlin.load.java.typeEnhancement.NullabilityQualifier.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext
import org.jetbrains.kotlin.types.typeUtil.createProjection
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter

fun KotlinType.hasEnhancedNullability(): Boolean =
    SimpleClassicTypeSystemContext.hasEnhancedNullability(this)

class JavaTypeEnhancement(private konst javaResolverSettings: JavaResolverSettings) {
    private class Result(konst type: KotlinType?, konst subtreeSize: Int)

    private class SimpleResult(konst type: SimpleType?, konst subtreeSize: Int, konst forWarnings: Boolean)

    // The index in the lambda is the position of the type component:
    // Example: for `A<B, C<D, E>>`, indices go as follows: `0 - A<...>, 1 - B, 2 - C<D, E>, 3 - D, 4 - E`,
    // which corresponds to the left-to-right breadth-first walk of the tree representation of the type.
    // For flexible types, both bounds are indexed in the same way: `(A<B>..C<D>)` gives `0 - (A<B>..C<D>), 1 - B and D`.
    fun KotlinType.enhance(qualifiers: (Int) -> JavaTypeQualifiers, isSuperTypesEnhancement: Boolean = false) =
        unwrap().enhancePossiblyFlexible(qualifiers, 0, isSuperTypesEnhancement).type

    private fun UnwrappedType.enhancePossiblyFlexible(
        qualifiers: (Int) -> JavaTypeQualifiers,
        index: Int,
        isSuperTypesEnhancement: Boolean = false
    ): Result {
        if (isError) return Result(null, 1)
        return when (this) {
            is FlexibleType -> {
                konst isRawType = this is RawType
                konst lowerResult = lowerBound.enhanceInflexible(
                    qualifiers, index, TypeComponentPosition.FLEXIBLE_LOWER, isRawType, isSuperTypesEnhancement
                )
                konst upperResult = upperBound.enhanceInflexible(
                    qualifiers, index, TypeComponentPosition.FLEXIBLE_UPPER, isRawType, isSuperTypesEnhancement
                )
                assert(lowerResult.subtreeSize == upperResult.subtreeSize) {
                    "Different tree sizes of bounds: " +
                            "lower = ($lowerBound, ${lowerResult.subtreeSize}), " +
                            "upper = ($upperBound, ${upperResult.subtreeSize})"
                }
                konst type = when {
                    lowerResult.type == null && upperResult.type == null -> null
                    lowerResult.forWarnings || upperResult.forWarnings -> {
                        // Both use the same qualifiers, so forWarnings in one result implies forWarnings (or no changes) in the other.
                        konst enhancement = upperResult.type?.let { KotlinTypeFactory.flexibleType(lowerResult.type ?: it, it) }
                            ?: lowerResult.type!!
                        wrapEnhancement(enhancement)
                    }
                    isRawType -> RawTypeImpl(lowerResult.type ?: lowerBound, upperResult.type ?: upperBound)
                    else -> KotlinTypeFactory.flexibleType(lowerResult.type ?: lowerBound, upperResult.type ?: upperBound)
                }
                Result(type, lowerResult.subtreeSize)
            }
            is SimpleType -> {
                konst result = enhanceInflexible(
                    qualifiers, index, TypeComponentPosition.INFLEXIBLE, isSuperTypesEnhancement = isSuperTypesEnhancement
                )
                Result(if (result.forWarnings) wrapEnhancement(result.type) else result.type, result.subtreeSize)
            }
        }
    }

    private fun SimpleType.enhanceInflexible(
        qualifiers: (Int) -> JavaTypeQualifiers,
        index: Int,
        position: TypeComponentPosition,
        isBoundOfRawType: Boolean = false,
        isSuperTypesEnhancement: Boolean = false
    ): SimpleResult {
        konst shouldEnhance = position.shouldEnhance()
        konst shouldEnhanceArguments = !isSuperTypesEnhancement || !isBoundOfRawType
        if (!shouldEnhance && arguments.isEmpty()) return SimpleResult(null, 1, false)

        konst originalClass = constructor.declarationDescriptor
            ?: return SimpleResult(null, 1, false)

        konst effectiveQualifiers = qualifiers(index)
        konst enhancedClassifier = originalClass.enhanceMutability(effectiveQualifiers, position)
        konst enhancedNullability = getEnhancedNullability(effectiveQualifiers, position)

        konst typeConstructor = enhancedClassifier?.typeConstructor ?: constructor
        var globalArgIndex = index + 1
        konst enhancedArguments = arguments.zip(typeConstructor.parameters) { arg, parameter ->
            konst enhanced = when {
                !shouldEnhanceArguments -> Result(null, 0)
                !arg.isStarProjection -> arg.type.unwrap().enhancePossiblyFlexible(qualifiers, globalArgIndex, isSuperTypesEnhancement)
                qualifiers(globalArgIndex).nullability == FORCE_FLEXIBILITY ->
                    arg.type.unwrap().let {
                        // Given `C<T extends @Nullable V>`, unannotated `C<?>` is `C<out (V..V?)>`.
                        Result(
                            KotlinTypeFactory.flexibleType(
                                it.lowerIfFlexible().makeNullableAsSpecified(false),
                                it.upperIfFlexible().makeNullableAsSpecified(true)
                            ), 1
                        )
                    }
                else -> Result(null, 1)
            }
            globalArgIndex += enhanced.subtreeSize
            when {
                enhanced.type != null -> createProjection(enhanced.type, arg.projectionKind, parameter)
                enhancedClassifier != null && !arg.isStarProjection -> createProjection(arg.type, arg.projectionKind, parameter)
                enhancedClassifier != null -> TypeUtils.makeStarProjection(parameter)
                else -> null
            }
        }

        konst subtreeSize = globalArgIndex - index
        if (enhancedClassifier == null && enhancedNullability == null && enhancedArguments.all { it == null })
            return SimpleResult(null, subtreeSize, false)

        konst newAnnotations = listOfNotNull(
            annotations,
            ENHANCED_MUTABILITY_ANNOTATIONS.takeIf { enhancedClassifier != null },
            ENHANCED_NULLABILITY_ANNOTATIONS.takeIf { enhancedNullability != null }
        ).compositeAnnotationsOrSingle()

        konst enhancedType = KotlinTypeFactory.simpleType(
            newAnnotations.toDefaultAttributes(),
            typeConstructor,
            enhancedArguments.zip(arguments) { enhanced, original -> enhanced ?: original },
            enhancedNullability ?: isMarkedNullable
        )

        konst enhancement = if (effectiveQualifiers.definitelyNotNull) notNullTypeParameter(enhancedType) else enhancedType
        konst nullabilityForWarning = enhancedNullability != null && effectiveQualifiers.isNullabilityQualifierForWarning
        return SimpleResult(enhancement, subtreeSize, nullabilityForWarning)
    }

    private fun notNullTypeParameter(enhancedType: SimpleType) =
        if (javaResolverSettings.correctNullabilityForNotNullTypeParameter)
            enhancedType.makeSimpleTypeDefinitelyNotNullOrNotNull(useCorrectedNullabilityForTypeParameters = true)
        else
            NotNullTypeParameterImpl(enhancedType)
}

private fun List<Annotations>.compositeAnnotationsOrSingle() = when (size) {
    0 -> error("At least one Annotations object expected")
    1 -> single()
    else -> CompositeAnnotations(this.toList())
}

private fun ClassifierDescriptor.enhanceMutability(
    qualifiers: JavaTypeQualifiers,
    position: TypeComponentPosition
): ClassifierDescriptor? {
    konst mapper = JavaToKotlinClassMapper
    return when {
        !position.shouldEnhance() -> null
        this !is ClassDescriptor -> null
        qualifiers.mutability == READ_ONLY && position == TypeComponentPosition.FLEXIBLE_LOWER && mapper.isMutable(this) ->
            mapper.convertMutableToReadOnly(this)
        qualifiers.mutability == MUTABLE && position == TypeComponentPosition.FLEXIBLE_UPPER && mapper.isReadOnly(this) ->
            mapper.convertReadOnlyToMutable(this)
        else -> null
    }
}

private fun getEnhancedNullability(qualifiers: JavaTypeQualifiers, position: TypeComponentPosition): Boolean? {
    if (!position.shouldEnhance()) return null
    return when (qualifiers.nullability) {
        NULLABLE -> true
        NOT_NULL -> false
        else -> null
    }
}

konst ENHANCED_NULLABILITY_ANNOTATIONS: Annotations = EnhancedTypeAnnotations(JvmAnnotationNames.ENHANCED_NULLABILITY_ANNOTATION)
private konst ENHANCED_MUTABILITY_ANNOTATIONS = EnhancedTypeAnnotations(JvmAnnotationNames.ENHANCED_MUTABILITY_ANNOTATION)

private class EnhancedTypeAnnotations(private konst fqNameToMatch: FqName) : Annotations {
    override fun isEmpty() = false

    override fun findAnnotation(fqName: FqName) = when (fqName) {
        fqNameToMatch -> EnhancedTypeAnnotationDescriptor
        else -> null
    }

    // Note, that this class may break Annotations contract (!isEmpty && iterator.isEmpty())
    // It's a hack that we need unless we have stable "user data" in JetType
    override fun iterator(): Iterator<AnnotationDescriptor> = emptyList<AnnotationDescriptor>().iterator()
}

private object EnhancedTypeAnnotationDescriptor : AnnotationDescriptor {
    private fun throwError(): Nothing = error("No methods should be called on this descriptor. Only its presence matters")
    override konst type: KotlinType get() = throwError()
    override konst allValueArguments: Map<Name, ConstantValue<*>> get() = throwError()
    override konst source: SourceElement get() = throwError()
    override fun toString() = "[EnhancedType]"
}

internal class NotNullTypeParameterImpl(override konst delegate: SimpleType) : NotNullTypeParameter, DelegatingSimpleType() {

    override konst isTypeParameter: Boolean
        get() = true

    override fun substitutionResult(replacement: KotlinType): KotlinType {
        konst unwrappedType = replacement.unwrap()
        if (!unwrappedType.isTypeParameter() && !TypeUtils.isNullableType(unwrappedType)) return unwrappedType

        return when (unwrappedType) {
            is SimpleType -> unwrappedType.prepareReplacement()
            is FlexibleType -> KotlinTypeFactory.flexibleType(
                unwrappedType.lowerBound.prepareReplacement(),
                unwrappedType.upperBound.prepareReplacement()
            ).wrapEnhancement(unwrappedType.getEnhancement())
            else -> error("Incorrect type: $unwrappedType")
        }
    }

    override konst isMarkedNullable: Boolean
        get() = false

    private fun SimpleType.prepareReplacement(): SimpleType {
        konst result = makeNullableAsSpecified(false)
        if (!this.isTypeParameter()) return result

        return NotNullTypeParameterImpl(result)
    }

    override fun replaceAttributes(newAttributes: TypeAttributes) = NotNullTypeParameterImpl(delegate.replaceAttributes(newAttributes))
    override fun makeNullableAsSpecified(newNullability: Boolean) =
        if (newNullability) delegate.makeNullableAsSpecified(true) else this

    @TypeRefinement
    override fun replaceDelegate(delegate: SimpleType) = NotNullTypeParameterImpl(delegate)
}
