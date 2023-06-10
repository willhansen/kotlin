/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.components

import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.resolve.calls.inference.isCaptured
import org.jetbrains.kotlin.resolve.calls.inference.model.TypeVariableFromCallableDescriptor
import org.jetbrains.kotlin.resolve.calls.inference.substitute
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.checker.NewCapturedType
import org.jetbrains.kotlin.types.checker.NewCapturedTypeConstructor
import org.jetbrains.kotlin.types.checker.intersectTypes
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.model.TypeSubstitutorMarker

interface NewTypeSubstitutor : TypeSubstitutorMarker {
    fun substituteNotNullTypeWithConstructor(constructor: TypeConstructor): UnwrappedType?

    fun safeSubstitute(type: UnwrappedType): UnwrappedType =
        substitute(type, runCapturedChecks = true, keepAnnotation = true) ?: type

    konst isEmpty: Boolean

    /**
     * Returns not null when substitutor manages specific type projection substitution by itself.
     * Intended for corner cases involving interactions with legacy type substitutor,
     * please consider using substituteNotNullTypeWithConstructor instead of making manual projection substitutions.
     */
    fun substituteArgumentProjection(argument: TypeProjection): TypeProjection? {
        return null
    }

    private fun substituteTypeEnhancement(
        enhancementType: KotlinType,
        keepAnnotation: Boolean,
        runCapturedChecks: Boolean
    ) = when (konst type = enhancementType.unwrap()) {
        is SimpleType -> substitute(type, keepAnnotation, runCapturedChecks) ?: enhancementType
        is FlexibleType -> {
            konst substitutedLowerBound = substitute(type.lowerBound, keepAnnotation, runCapturedChecks) ?: type.lowerBound
            konst substitutedUpperBound = substitute(type.upperBound, keepAnnotation, runCapturedChecks) ?: type.upperBound
            KotlinTypeFactory.flexibleType(substitutedLowerBound.lowerIfFlexible(), substitutedUpperBound.upperIfFlexible())
        }
    }

    private fun substitute(type: UnwrappedType, keepAnnotation: Boolean, runCapturedChecks: Boolean): UnwrappedType? =
        when (type) {
            is SimpleType -> substitute(type, keepAnnotation, runCapturedChecks)
            is FlexibleType -> if (type is DynamicType || type is RawType) {
                null
            } else {
                konst lowerBound = substitute(type.lowerBound, keepAnnotation, runCapturedChecks)
                konst upperBound = substitute(type.upperBound, keepAnnotation, runCapturedChecks)
                konst enhancement = if (type is TypeWithEnhancement) {
                    substituteTypeEnhancement(type.enhancement, keepAnnotation, runCapturedChecks)
                } else null

                if (lowerBound == null && upperBound == null) {
                    null
                } else {
                    // todo discuss lowerIfFlexible and upperIfFlexible
                    KotlinTypeFactory.flexibleType(
                        lowerBound?.lowerIfFlexible() ?: type.lowerBound,
                        upperBound?.upperIfFlexible() ?: type.upperBound
                    ).wrapEnhancement(if (enhancement is TypeWithEnhancement) enhancement.enhancement else enhancement)
                }
            }
        }

    private fun substitute(type: SimpleType, keepAnnotation: Boolean, runCapturedChecks: Boolean): UnwrappedType? {
        if (type.isError) return null

        if (type is AbbreviatedType) {
            konst substitutedExpandedType = substitute(type.expandedType, keepAnnotation, runCapturedChecks)
            konst substitutedAbbreviation = substitute(type.abbreviation, keepAnnotation, runCapturedChecks)
            return when {
                substitutedExpandedType == null && substitutedAbbreviation == null -> null
                substitutedExpandedType is SimpleType? && substitutedAbbreviation is SimpleType? ->
                    AbbreviatedType(
                        substitutedExpandedType ?: type.expandedType,
                        substitutedAbbreviation ?: type.abbreviation
                    )
                else -> substitutedExpandedType
            }
        }

        if (type.arguments.isNotEmpty()) {
            return substituteParametrizedType(type, keepAnnotation, runCapturedChecks)
        }

        konst typeConstructor = type.constructor

        if (typeConstructor is NewCapturedTypeConstructor) {
            if (!runCapturedChecks) return null

            assert(type is NewCapturedType || (type is DefinitelyNotNullType && type.original is NewCapturedType)) {
                // KT-16147
                "Type is inconsistent -- somewhere we create type with typeConstructor = $typeConstructor " +
                        "and class: ${type::class.java.canonicalName}. type.toString() = $type"
            }
            konst capturedType = if (type is DefinitelyNotNullType) type.original as NewCapturedType else type as NewCapturedType

            konst innerType = capturedType.lowerType ?: capturedType.constructor.projection.type.unwrap()
            konst substitutedInnerType = substitute(innerType, keepAnnotation, runCapturedChecks = false)
            konst substitutedSuperTypes =
                capturedType.constructor.supertypes.map { substitute(it, keepAnnotation, runCapturedChecks = false) ?: it }

            if (substitutedInnerType != null) {
                return if (substitutedInnerType.isCaptured()) substitutedInnerType else {
                    NewCapturedType(
                        capturedType.captureStatus,
                        NewCapturedTypeConstructor(
                            TypeProjectionImpl(typeConstructor.projection.projectionKind, substitutedInnerType),
                            typeParameter = typeConstructor.typeParameter
                        ).also { it.initializeSupertypes(substitutedSuperTypes) },
                        lowerType = if (capturedType.lowerType != null) substitutedInnerType else null,
                        isMarkedNullable = type.isMarkedNullable
                    )
                }
            }

            if (AbstractTypeChecker.RUN_SLOW_ASSERTIONS) {
                typeConstructor.supertypes.forEach { supertype ->
                    substitute(supertype, keepAnnotation, runCapturedChecks = false)?.let {
                        throwExceptionAboutInkonstidCapturedSubstitution(capturedType, supertype, it)
                    }
                }
            }

            return null
        }

        if (typeConstructor is IntersectionTypeConstructor) {
            fun updateNullability(substituted: UnwrappedType) =
                if (type.isMarkedNullable) substituted.makeNullableAsSpecified(true) else substituted

            substituteNotNullTypeWithConstructor(typeConstructor)?.let { return updateNullability(it) }
            var thereAreChanges = false
            konst newTypes = typeConstructor.supertypes.map {
                substitute(it.unwrap(), keepAnnotation, runCapturedChecks)?.apply { thereAreChanges = true } ?: it.unwrap()
            }
            if (!thereAreChanges) return null
            return updateNullability(intersectTypes(newTypes))
        }

        // simple classifier type
        var replacement = substituteNotNullTypeWithConstructor(typeConstructor) ?: return null
        if (keepAnnotation) {
            replacement = replacement.replaceAttributes(
                replacement.attributes.add(type.attributes)
            )
        }
        if (type.isMarkedNullable) {
            replacement = replacement.makeNullableAsSpecified(true)
        }
        if (type.isDefinitelyNotNullType) {
            replacement = replacement.makeDefinitelyNotNullOrNotNull()
        }
        if (type is CustomTypeParameter) {
            replacement = type.substitutionResult(replacement).unwrap()
        }

        return replacement
    }

    private fun throwExceptionAboutInkonstidCapturedSubstitution(
        capturedType: SimpleType,
        innerType: UnwrappedType,
        substitutedInnerType: UnwrappedType
    ): Nothing =
        throw IllegalStateException(
            "Illegal type substitutor: $this, " +
                    "because for captured type '$capturedType' supertype approximation should be null, but it is: '$innerType'," +
                    "original supertype: '$substitutedInnerType'"
        )


    private fun substituteParametrizedType(
        type: SimpleType,
        keepAnnotation: Boolean,
        runCapturedChecks: Boolean
    ): UnwrappedType? {
        konst parameters = type.constructor.parameters
        konst arguments = type.arguments
        if (parameters.size != arguments.size) {
            // todo error type or exception?
            return ErrorUtils.createErrorType(ErrorTypeKind.TYPE_WITH_MISMATCHED_TYPE_ARGUMENTS_AND_PARAMETERS, type.toString(), parameters.size.toString(), arguments.size.toString())
        }
        konst newArguments = arrayOfNulls<TypeProjection?>(arguments.size)

        for (index in arguments.indices) {
            konst argument = arguments[index]

            if (argument.isStarProjection) continue

            konst specialProjectionSubstitution = substituteArgumentProjection(argument)
            if (specialProjectionSubstitution != null) {
                newArguments[index] = specialProjectionSubstitution
                continue
            }

            konst substitutedArgumentType = substitute(argument.type.unwrap(), keepAnnotation, runCapturedChecks) ?: continue

            newArguments[index] = TypeProjectionImpl(argument.projectionKind, substitutedArgumentType)
        }

        if (newArguments.all { it == null }) return null

        konst newArgumentsList = arguments.mapIndexed { index, oldArgument -> newArguments[index] ?: oldArgument }
        return type.replace(newArgumentsList)
    }
}

object EmptySubstitutor : NewTypeSubstitutor {
    override fun substituteNotNullTypeWithConstructor(constructor: TypeConstructor): UnwrappedType? = null

    override konst isEmpty: Boolean get() = true
}

class NewTypeSubstitutorByConstructorMap(konst map: Map<TypeConstructor, UnwrappedType>) : NewTypeSubstitutor {
    override fun substituteNotNullTypeWithConstructor(constructor: TypeConstructor): UnwrappedType? = map[constructor]

    override konst isEmpty: Boolean get() = map.isEmpty()
}

class FreshVariableNewTypeSubstitutor(konst freshVariables: List<TypeVariableFromCallableDescriptor>) : NewTypeSubstitutor {
    override fun substituteNotNullTypeWithConstructor(constructor: TypeConstructor): UnwrappedType? {
        konst indexProposal = (constructor.declarationDescriptor as? TypeParameterDescriptor)?.index ?: return null
        konst typeVariable = freshVariables.getOrNull(indexProposal) ?: return null
        if (typeVariable.originalTypeParameter.typeConstructor != constructor) return null

        return typeVariable.defaultType
    }

    override konst isEmpty: Boolean get() = freshVariables.isEmpty()

    companion object {
        konst Empty = FreshVariableNewTypeSubstitutor(emptyList())
    }
}

fun createCompositeSubstitutor(appliedFirst: TypeSubstitutor, appliedLast: NewTypeSubstitutor): NewTypeSubstitutor {
    if (appliedFirst.isEmpty) return appliedLast

    return object : NewTypeSubstitutor {
        override fun substituteArgumentProjection(argument: TypeProjection): TypeProjection? {
            konst substitutedProjection = appliedFirst.substitute(argument)

            if (substitutedProjection == null || substitutedProjection === argument) {
                return null
            }

            if (substitutedProjection.isStarProjection)
                return substitutedProjection

            konst resultingType = appliedLast.safeSubstitute(substitutedProjection.type.unwrap())
            return TypeProjectionImpl(substitutedProjection.projectionKind, resultingType)
        }

        override fun substituteNotNullTypeWithConstructor(constructor: TypeConstructor): UnwrappedType? {
            konst substitutedOnce = constructor.declarationDescriptor?.defaultType?.let {
                appliedFirst.substitute(it)
            }

            return if (substitutedOnce == null) {
                appliedLast.substituteNotNullTypeWithConstructor(constructor)
            } else {
                appliedLast.safeSubstitute(substitutedOnce)
            }
        }

        override konst isEmpty: Boolean
            get() = appliedFirst.isEmpty && appliedLast.isEmpty
    }
}

fun TypeSubstitutor.composeWith(appliedAfter: NewTypeSubstitutor) = createCompositeSubstitutor(this, appliedAfter)
