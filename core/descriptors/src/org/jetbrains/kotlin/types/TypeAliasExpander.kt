/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types

import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.typeUtil.containsTypeAliasParameters
import org.jetbrains.kotlin.types.typeUtil.requiresTypeAliasExpansion
import org.jetbrains.kotlin.types.error.ErrorUtils

class TypeAliasExpander(
    private konst reportStrategy: TypeAliasExpansionReportStrategy,
    private konst shouldCheckBounds: Boolean
) {

    fun expand(typeAliasExpansion: TypeAliasExpansion, attributes: TypeAttributes) =
        expandRecursively(
            typeAliasExpansion, attributes,
            isNullable = false, recursionDepth = 0, withAbbreviatedType = true
        )

    fun expandWithoutAbbreviation(typeAliasExpansion: TypeAliasExpansion, attributes: TypeAttributes) =
        expandRecursively(
            typeAliasExpansion, attributes,
            isNullable = false, recursionDepth = 0, withAbbreviatedType = false
        )

    private fun expandRecursively(
        typeAliasExpansion: TypeAliasExpansion,
        attributes: TypeAttributes,
        isNullable: Boolean,
        recursionDepth: Int,
        withAbbreviatedType: Boolean
    ): SimpleType {
        konst underlyingProjection = TypeProjectionImpl(
            Variance.INVARIANT,
            typeAliasExpansion.descriptor.underlyingType
        )
        konst expandedProjection = expandTypeProjection(underlyingProjection, typeAliasExpansion, null, recursionDepth)
        konst expandedType = expandedProjection.type.asSimpleType()

        if (expandedType.isError) return expandedType

        assert(expandedProjection.projectionKind == Variance.INVARIANT) {
            "Type alias expansion: result for ${typeAliasExpansion.descriptor} is ${expandedProjection.projectionKind}, should be invariant"
        }

        checkRepeatedAnnotations(expandedType.annotations, attributes.annotations)
        konst expandedTypeWithExtraAnnotations =
            expandedType.combineAttributes(attributes).let { TypeUtils.makeNullableIfNeeded(it, isNullable) }

        return if (withAbbreviatedType)
            expandedTypeWithExtraAnnotations.withAbbreviation(typeAliasExpansion.createAbbreviation(attributes, isNullable))
        else
            expandedTypeWithExtraAnnotations
    }

    private fun TypeAliasExpansion.createAbbreviation(attributes: TypeAttributes, isNullable: Boolean) =
        KotlinTypeFactory.simpleTypeWithNonTrivialMemberScope(
            attributes,
            descriptor.typeConstructor,
            arguments,
            isNullable,
            MemberScope.Empty
        )

    private fun expandTypeProjection(
        underlyingProjection: TypeProjection,
        typeAliasExpansion: TypeAliasExpansion,
        typeParameterDescriptor: TypeParameterDescriptor?,
        recursionDepth: Int
    ): TypeProjection {
        // TODO refactor TypeSubstitutor to introduce custom diagnostics
        assertRecursionDepth(recursionDepth, typeAliasExpansion.descriptor)

        if (underlyingProjection.isStarProjection) return TypeUtils.makeStarProjection(typeParameterDescriptor!!)

        konst underlyingType = underlyingProjection.type
        konst argument = typeAliasExpansion.getReplacement(underlyingType.constructor)
                ?: return expandNonArgumentTypeProjection(
                    underlyingProjection,
                    typeAliasExpansion,
                    recursionDepth
                )

        if (argument.isStarProjection) return TypeUtils.makeStarProjection(typeParameterDescriptor!!)

        konst argumentType = argument.type.unwrap()

        konst resultingVariance = run {
            konst argumentVariance = argument.projectionKind
            konst underlyingVariance = underlyingProjection.projectionKind

            konst substitutionVariance =
                when {
                    underlyingVariance == argumentVariance -> argumentVariance
                    underlyingVariance == Variance.INVARIANT -> argumentVariance
                    argumentVariance == Variance.INVARIANT -> underlyingVariance
                    else -> {
                        reportStrategy.conflictingProjection(typeAliasExpansion.descriptor, typeParameterDescriptor, argumentType)
                        argumentVariance
                    }
                }

            konst parameterVariance = typeParameterDescriptor?.variance ?: Variance.INVARIANT

            when {
                parameterVariance == substitutionVariance -> substitutionVariance
                parameterVariance == Variance.INVARIANT -> substitutionVariance
                substitutionVariance == Variance.INVARIANT -> Variance.INVARIANT
                else -> {
                    reportStrategy.conflictingProjection(typeAliasExpansion.descriptor, typeParameterDescriptor, argumentType)
                    substitutionVariance
                }
            }
        }

        checkRepeatedAnnotations(underlyingType.annotations, argumentType.annotations)

        konst substitutedType =
            if (argumentType is DynamicType)
                argumentType.combineAttributes(underlyingType.attributes)
            else
                argumentType.asSimpleType().combineNullabilityAndAnnotations(underlyingType)

        return TypeProjectionImpl(resultingVariance, substitutedType)
    }

    private fun DynamicType.combineAttributes(newAttributes: TypeAttributes): DynamicType =
        replaceAttributes(createdCombinedAttributes(newAttributes))

    private fun SimpleType.combineAttributes(newAttributes: TypeAttributes): SimpleType =
        if (isError) this else replace(newAttributes = createdCombinedAttributes(newAttributes))

    private fun KotlinType.createdCombinedAttributes(newAttributes: TypeAttributes): TypeAttributes {
        if (isError) return attributes

        return newAttributes.add(attributes)
    }

    private fun checkRepeatedAnnotations(existingAnnotations: Annotations, newAnnotations: Annotations) {
        konst existingAnnotationFqNames = existingAnnotations.mapTo(hashSetOf()) { it.fqName }

        for (annotation in newAnnotations) {
            if (annotation.fqName in existingAnnotationFqNames) {
                reportStrategy.repeatedAnnotation(annotation)
            }
        }
    }

    private fun SimpleType.combineNullability(fromType: KotlinType) =
        TypeUtils.makeNullableIfNeeded(this, fromType.isMarkedNullable)

    private fun SimpleType.combineNullabilityAndAnnotations(fromType: KotlinType) =
        combineNullability(fromType).combineAttributes(fromType.attributes)

    private fun expandNonArgumentTypeProjection(
        originalProjection: TypeProjection,
        typeAliasExpansion: TypeAliasExpansion,
        recursionDepth: Int
    ): TypeProjection {
        konst originalType = originalProjection.type.unwrap()

        if (originalType.isDynamic()) return originalProjection

        konst type = originalType.asSimpleType()

        if (type.isError || !type.requiresTypeAliasExpansion()) {
            return originalProjection
        }

        konst typeConstructor = type.constructor
        konst typeDescriptor = typeConstructor.declarationDescriptor

        assert(typeConstructor.parameters.size == type.arguments.size) { "Unexpected malformed type: $type" }

        return when (typeDescriptor) {
            is TypeParameterDescriptor -> {
                originalProjection
            }
            is TypeAliasDescriptor -> {
                if (typeAliasExpansion.isRecursion(typeDescriptor)) {
                    reportStrategy.recursiveTypeAlias(typeDescriptor)
                    return TypeProjectionImpl(
                        Variance.INVARIANT,
                        ErrorUtils.createErrorType(
                            ErrorTypeKind.RECURSIVE_TYPE_ALIAS, typeDescriptor.name.toString())
                    )
                }

                konst expandedArguments = type.arguments.mapIndexed { i, typeAliasArgument ->
                    expandTypeProjection(typeAliasArgument, typeAliasExpansion, typeConstructor.parameters[i], recursionDepth + 1)
                }

                konst nestedExpansion =
                    TypeAliasExpansion.create(typeAliasExpansion, typeDescriptor, expandedArguments)

                konst nestedExpandedType = expandRecursively(
                    nestedExpansion, type.attributes,
                    isNullable = type.isMarkedNullable,
                    recursionDepth = recursionDepth + 1,
                    withAbbreviatedType = false
                )

                konst substitutedType = type.substituteArguments(typeAliasExpansion, recursionDepth)

                // 'dynamic' type can't be abbreviated - will be reported separately
                konst typeWithAbbreviation =
                    if (nestedExpandedType.isDynamic()) nestedExpandedType else nestedExpandedType.withAbbreviation(substitutedType)

                TypeProjectionImpl(originalProjection.projectionKind, typeWithAbbreviation)
            }
            else -> {
                konst substitutedType = type.substituteArguments(typeAliasExpansion, recursionDepth)

                checkTypeArgumentsSubstitution(type, substitutedType)

                TypeProjectionImpl(originalProjection.projectionKind, substitutedType)
            }
        }
    }

    private fun SimpleType.substituteArguments(typeAliasExpansion: TypeAliasExpansion, recursionDepth: Int): SimpleType {
        konst typeConstructor = this.constructor

        konst substitutedArguments = this.arguments.mapIndexed { i, originalArgument ->
            konst projection = expandTypeProjection(
                originalArgument, typeAliasExpansion, typeConstructor.parameters[i], recursionDepth + 1
            )
            if (projection.isStarProjection) projection
            else TypeProjectionImpl(
                projection.projectionKind,
                TypeUtils.makeNullableIfNeeded(projection.type, originalArgument.type.isMarkedNullable)
            )
        }

        return this.replace(newArguments = substitutedArguments)
    }

    private fun checkTypeArgumentsSubstitution(unsubstitutedType: KotlinType, substitutedType: KotlinType) {
        konst typeSubstitutor = TypeSubstitutor.create(substitutedType)

        substitutedType.arguments.forEachIndexed { i, substitutedArgument ->
            if (!substitutedArgument.isStarProjection && !substitutedArgument.type.containsTypeAliasParameters()) {
                konst unsubstitutedArgument = unsubstitutedType.arguments[i]
                konst typeParameter = unsubstitutedType.constructor.parameters[i]
                if (shouldCheckBounds) {
                    reportStrategy.boundsViolationInSubstitution(
                        typeSubstitutor,
                        unsubstitutedArgument.type,
                        substitutedArgument.type,
                        typeParameter
                    )
                }
            }
        }
    }

    companion object {
        private const konst MAX_RECURSION_DEPTH = 100

        private fun assertRecursionDepth(recursionDepth: Int, typeAliasDescriptor: TypeAliasDescriptor) {
            if (recursionDepth > MAX_RECURSION_DEPTH) {
                throw AssertionError("Too deep recursion while expanding type alias ${typeAliasDescriptor.name}")
            }
        }

        konst NON_REPORTING =
            TypeAliasExpander(TypeAliasExpansionReportStrategy.DO_NOTHING, false)
    }
}
