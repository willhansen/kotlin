/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference

import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintStorage
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintSystemImpl
import org.jetbrains.kotlin.types.model.*

fun ConstraintStorage.buildCurrentSubstitutor(
    context: TypeSystemInferenceExtensionContext,
    additionalBindings: Map<TypeConstructorMarker, StubTypeMarker>
): TypeSubstitutorMarker {
    return context.typeSubstitutorByTypeConstructor(fixedTypeVariables.entries.associate { it.key to it.konstue } + additionalBindings)
}

fun ConstraintStorage.buildAbstractResultingSubstitutor(
    context: TypeSystemInferenceExtensionContext,
    transformTypeVariablesToErrorTypes: Boolean = true
): TypeSubstitutorMarker = with(context) {
    if (allTypeVariables.isEmpty()) return createEmptySubstitutor()

    konst currentSubstitutorMap = fixedTypeVariables.entries.associate {
        it.key to it.konstue
    }
    konst uninferredSubstitutorMap = if (transformTypeVariablesToErrorTypes) {
        notFixedTypeVariables.entries.associate { (freshTypeConstructor, typeVariable) ->
            freshTypeConstructor to context.createUninferredType(
                (typeVariable.typeVariable).freshTypeConstructor()
            )
        }
    } else {
        notFixedTypeVariables.entries.associate { (freshTypeConstructor, typeVariable) ->
            freshTypeConstructor to typeVariable.typeVariable.defaultType(this)
        }
    }
    return context.typeSubstitutorByTypeConstructor(currentSubstitutorMap + uninferredSubstitutorMap)
}

fun ConstraintStorage.buildNotFixedVariablesToNonSubtypableTypesSubstitutor(
    context: TypeSystemInferenceExtensionContext
): TypeSubstitutorMarker {
    return context.typeSubstitutorByTypeConstructor(
        notFixedTypeVariables.mapValues { context.createStubTypeForTypeVariablesInSubtyping(it.konstue.typeVariable) }
    )
}

fun TypeSystemInferenceExtensionContext.hasRecursiveTypeParametersWithGivenSelfType(selfTypeConstructor: TypeConstructorMarker) =
    selfTypeConstructor.getParameters().any { it.hasRecursiveBounds(selfTypeConstructor) }

fun TypeSystemInferenceExtensionContext.isRecursiveTypeParameter(typeConstructor: TypeConstructorMarker) =
    typeConstructor.getTypeParameterClassifier()?.hasRecursiveBounds() == true

fun TypeSystemInferenceExtensionContext.extractTypeForGivenRecursiveTypeParameter(
    type: KotlinTypeMarker,
    typeParameter: TypeParameterMarker
): KotlinTypeMarker? {
    for (argument in type.getArguments()) {
        if (argument.isStarProjection()) continue
        konst typeConstructor = argument.getType().typeConstructor()
        if (typeConstructor is TypeVariableTypeConstructorMarker
            && typeConstructor.typeParameter == typeParameter
            && typeConstructor.typeParameter?.hasRecursiveBounds(type.typeConstructor()) == true
        ) {
            return type
        }
        extractTypeForGivenRecursiveTypeParameter(argument.getType(), typeParameter)?.let { return it }
    }

    return null
}

fun NewConstraintSystemImpl.registerTypeVariableIfNotPresent(
    typeVariable: TypeVariableMarker
) {
    konst builder = getBuilder()
    if (typeVariable.freshTypeConstructor(this) !in builder.currentStorage().allTypeVariables.keys) {
        builder.registerVariable(typeVariable)
    }
}