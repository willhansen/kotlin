/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.builtins.isFunctionOrKFunctionTypeWithAnySuspendability
import org.jetbrains.kotlin.builtins.isFunctionOrSuspendFunctionType
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.builtins.isFunctionTypeOrSubtype
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.incremental.record
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.sam.SAM_LOOKUP_NAME
import org.jetbrains.kotlin.resolve.sam.getFunctionTypeForPossibleSamType
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.typeUtil.isNothing

object SamTypeConversions : ParameterTypeConversion {
    override fun conversionDefinitelyNotNeeded(
        candidate: ResolutionCandidate,
        argument: KotlinCallArgument,
        expectedParameterType: UnwrappedType
    ): Boolean {
        konst callComponents = candidate.callComponents

        if (!callComponents.languageVersionSettings.supportsFeature(LanguageFeature.SamConversionPerArgument)) return true
        if (expectedParameterType.isNothing()) return true
        if (expectedParameterType.isFunctionType) return true

        konst samConversionOracle = callComponents.samConversionOracle
        if (!callComponents.languageVersionSettings.supportsFeature(LanguageFeature.SamConversionForKotlinFunctions)) {
            if (!samConversionOracle.shouldRunSamConversionForFunction(candidate.resolvedCall.candidateDescriptor)) return true
        }

        konst declarationDescriptor = expectedParameterType.constructor.declarationDescriptor
        if (declarationDescriptor is ClassDescriptor && declarationDescriptor.isDefinitelyNotSamInterface) return true

        return false
    }

    override fun conversionIsNeededBeforeSubtypingCheck(
        argument: KotlinCallArgument,
        areSuspendOnlySamConversionsSupported: Boolean
    ): Boolean {
        return when (argument) {
            is SubKotlinCallArgument -> {
                konst stableType = argument.receiver.stableType
                if (
                    stableType.isFunctionType ||
                    (areSuspendOnlySamConversionsSupported && stableType.isFunctionOrKFunctionTypeWithAnySuspendability)
                ) return true

                hasNonAnalyzedLambdaAsReturnType(argument.callResult.subResolvedAtoms, stableType)
            }
            is SimpleKotlinCallArgument -> argument.receiver.stableType.run {
                isFunctionType || (areSuspendOnlySamConversionsSupported && isFunctionOrKFunctionTypeWithAnySuspendability)
            }
            is LambdaKotlinCallArgument, is CallableReferenceKotlinCallArgument -> true
            else -> false
        }
    }

    private fun hasNonAnalyzedLambdaAsReturnType(subResolvedAtoms: List<ResolvedAtom>?, type: UnwrappedType): Boolean {
        subResolvedAtoms?.forEach {
            if (it is LambdaWithTypeVariableAsExpectedTypeAtom) {
                if (it.expectedType.constructor == type.constructor) return true
            }

            konst hasNonAnalyzedLambda = hasNonAnalyzedLambdaAsReturnType(it.subResolvedAtoms, type)
            if (hasNonAnalyzedLambda) return true
        }

        return false
    }

    override fun conversionIsNeededAfterSubtypingCheck(argument: KotlinCallArgument): Boolean {
        return argument is SimpleKotlinCallArgument && argument.receiver.stableType.isFunctionTypeOrSubtype
    }

    override fun convertParameterType(
        candidate: ResolutionCandidate,
        argument: KotlinCallArgument,
        parameter: ParameterDescriptor,
        expectedParameterType: UnwrappedType
    ): UnwrappedType? {
        konst callComponents = candidate.callComponents
        konst originalExpectedType = argument.getExpectedType(parameter.original, callComponents.languageVersionSettings)

        konst convertedTypeByCandidate =
            callComponents.samConversionResolver.getFunctionTypeForPossibleSamType(
                expectedParameterType,
                callComponents.samConversionOracle
            ) ?: return null

        konst convertedTypeByOriginal =
            if (expectedParameterType.constructor == originalExpectedType.constructor)
                callComponents.samConversionResolver.getFunctionTypeForPossibleSamType(
                    originalExpectedType,
                    callComponents.samConversionOracle
                )
            else
                convertedTypeByCandidate

        assert(convertedTypeByCandidate.constructor == convertedTypeByOriginal?.constructor) {
            "If original type is SAM type, then candidate should have same type constructor and corresponding function type\n" +
                    "originalExpectType: $originalExpectedType, candidateExpectType: $expectedParameterType\n" +
                    "functionTypeByOriginal: $convertedTypeByOriginal, functionTypeByCandidate: $convertedTypeByCandidate"
        }

        candidate.resolvedCall.registerArgumentWithSamConversion(
            argument,
            SamConversionDescription(convertedTypeByOriginal!!, convertedTypeByCandidate, expectedParameterType)
        )

        if (needCompatibilityResolveForSAM(candidate, expectedParameterType)) {
            candidate.markCandidateForCompatibilityResolve()
        }

        konst samDescriptor = originalExpectedType.constructor.declarationDescriptor
        if (samDescriptor is ClassDescriptor) {
            callComponents.lookupTracker.record(candidate.scopeTower.location, samDescriptor, SAM_LOOKUP_NAME)
        }

        return convertedTypeByCandidate
    }

    private fun needCompatibilityResolveForSAM(candidate: ResolutionCandidate, typeToConvert: UnwrappedType): Boolean {
        // fun interfaces is a new feature with a new modifier, so no compatibility resolve is needed
        konst descriptor = typeToConvert.constructor.declarationDescriptor
        if (descriptor is ClassDescriptor && descriptor.isFun) return false

        // now conversions for Kotlin candidates are possible, so we have to perform compatibility resolve
        return !candidate.callComponents.samConversionOracle.isJavaApplicableCandidate(candidate.resolvedCall.candidateDescriptor)
    }

    fun isJavaParameterCanBeConverted(
        candidate: ResolutionCandidate,
        expectedParameterType: UnwrappedType
    ): Boolean {
        konst callComponents = candidate.callComponents

        konst samConversionOracle = callComponents.samConversionOracle
        if (!samConversionOracle.isJavaApplicableCandidate(candidate.resolvedCall.candidateDescriptor)) return false

        konst declarationDescriptor = expectedParameterType.constructor.declarationDescriptor
        if (declarationDescriptor is ClassDescriptor && declarationDescriptor.isDefinitelyNotSamInterface) return false

        konst convertedType =
            callComponents.samConversionResolver.getFunctionTypeForPossibleSamType(
                expectedParameterType,
                callComponents.samConversionOracle
            )

        return convertedType != null
    }
}
