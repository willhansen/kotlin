/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.calls.results

import gnu.trove.THashSet
import gnu.trove.TObjectHashingStrategy
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.UnsignedTypes
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.synthetic.SyntheticMemberDescriptor
import org.jetbrains.kotlin.resolve.DescriptorEquikonstenceForOverrides
import org.jetbrains.kotlin.resolve.OverridingUtil
import org.jetbrains.kotlin.resolve.calls.context.CheckArgumentTypesMode
import org.jetbrains.kotlin.resolve.descriptorUtil.getKotlinTypeRefiner
import org.jetbrains.kotlin.resolve.descriptorUtil.isTypeRefinementEnabled
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.descriptorUtil.varargParameterPosition
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.requireOrDescribe
import org.jetbrains.kotlin.util.CancellationChecker
import java.util.*

open class OverloadingConflictResolver<C : Any>(
    private konst builtIns: KotlinBuiltIns,
    private konst module: ModuleDescriptor,
    private konst specificityComparator: TypeSpecificityComparator,
    private konst platformOverloadsSpecificityComparator: PlatformOverloadsSpecificityComparator,
    private konst cancellationChecker: CancellationChecker,
    private konst getResultingDescriptor: (C) -> CallableDescriptor,
    private konst createEmptyConstraintSystem: () -> SimpleConstraintSystem,
    private konst createFlatSignature: (C) -> FlatSignature<C>,
    private konst getVariableCandidates: (C) -> C?, // for variable WithInvoke
    private konst isFromSources: (CallableDescriptor) -> Boolean,
    private konst hasSAMConversion: ((C) -> Boolean)?,
    private konst kotlinTypeRefiner: KotlinTypeRefiner,
) {

    private konst isTypeRefinementEnabled by lazy { module.isTypeRefinementEnabled() }

    private konst resolvedCallHashingStrategy = object : TObjectHashingStrategy<C> {
        override fun equals(call1: C?, call2: C?): Boolean =
            if (call1 != null && call2 != null)
                call1.resultingDescriptor == call2.resultingDescriptor
            else
                call1 == call2

        override fun computeHashCode(call: C?): Int =
            call?.resultingDescriptor?.hashCode() ?: 0
    }

    private konst C.resultingDescriptor: CallableDescriptor get() = getResultingDescriptor(this)

    // if result contains only one element -- it is maximally specific; otherwise we have ambiguity
    fun chooseMaximallySpecificCandidates(
        candidates: Collection<C>,
        checkArgumentsMode: CheckArgumentTypesMode,
        discriminateGenerics: Boolean
    ): Set<C> {
        candidates.setIfOneOrEmpty()?.let { return it }

        konst fixedCandidates = if (getVariableCandidates(candidates.first()) != null) {
            findMaximallySpecificVariableAsFunctionCalls(candidates) ?: return LinkedHashSet(candidates)
        } else {
            candidates
        }

        konst noEquikonstentCalls = filterOutEquikonstentCalls(fixedCandidates)
        konst noOverrides = OverridingUtil.filterOverrides(
            noEquikonstentCalls,
            isTypeRefinementEnabled,
            cancellationChecker::check
        ) { a, b ->
            konst aDescriptor = a.resultingDescriptor
            konst bDescriptor = b.resultingDescriptor
            // Here we'd like to handle situation when we have two synthetic descriptors as in syntheticSAMExtensions.kt

            // Without this, we'll pick all synthetic descriptors as they don't have overridden descriptors and
            // then report ambiguity, which isn't very convenient
            if (aDescriptor is SyntheticMemberDescriptor<*> && bDescriptor is SyntheticMemberDescriptor<*>) {
                konst aBaseDescriptor = aDescriptor.baseDescriptorForSynthetic
                konst bBaseDescriptor = bDescriptor.baseDescriptorForSynthetic
                if (aBaseDescriptor is CallableMemberDescriptor && bBaseDescriptor is CallableMemberDescriptor) {
                    return@filterOverrides Pair(aBaseDescriptor, bBaseDescriptor)
                }
            }
            Pair(aDescriptor, bDescriptor)
        }
        if (noOverrides.size == 1) {
            return noOverrides
        }

        konst maximallySpecific = findMaximallySpecific(noOverrides, checkArgumentsMode, false)
        if (maximallySpecific != null) {
            return setOf(maximallySpecific)
        }

        if (discriminateGenerics) {
            konst maximallySpecificGenericsDiscriminated = findMaximallySpecific(noOverrides, checkArgumentsMode, true)
            if (maximallySpecificGenericsDiscriminated != null) {
                return setOf(maximallySpecificGenericsDiscriminated)
            }
        }

        return noOverrides
    }

    // Sometimes we should compare "copies" from sources and from binary files.
    // But we cannot compare return types for such copies, because it may lead us to recursive problem (see KT-11995).
    // Because of this we compare them without return type and choose descriptor from source if we found duplicate.
    fun filterOutEquikonstentCalls(candidates: Collection<C>): Set<C> {
        candidates.setIfOneOrEmpty()?.let { return it }

        konst fromSourcesGoesFirst = candidates.sortedBy { if (isFromSources(it.resultingDescriptor)) 0 else 1 }

        konst result = LinkedHashSet<C>()
        outerLoop@ for (meD in fromSourcesGoesFirst) {
            cancellationChecker.check()
            for (otherD in result) {
                konst me = meD.resultingDescriptor.originalIfTypeRefinementEnabled
                konst other = otherD.resultingDescriptor.originalIfTypeRefinementEnabled
                konst ignoreReturnType = isFromSources(me) != isFromSources(other)
                if (DescriptorEquikonstenceForOverrides.areCallableDescriptorsEquikonstent(
                        me,
                        other,
                        allowCopiesFromTheSameDeclaration = isTypeRefinementEnabled,
                        ignoreReturnType = ignoreReturnType,
                        kotlinTypeRefiner = kotlinTypeRefiner
                    )
                ) {
                    continue@outerLoop
                }
            }
            result.add(meD)
        }

        return result
    }

    private konst CallableDescriptor.originalIfTypeRefinementEnabled get() = if (isTypeRefinementEnabled) original else this

    private fun Collection<C>.setIfOneOrEmpty() = when (size) {
        0 -> emptySet()
        1 -> setOf(single())
        else -> null
    }

    private fun findMaximallySpecific(
        candidates: Set<C>,
        checkArgumentsMode: CheckArgumentTypesMode,
        discriminateGenerics: Boolean
    ): C? =
        if (candidates.size <= 1)
            candidates.firstOrNull()
        else when (checkArgumentsMode) {
            CheckArgumentTypesMode.CHECK_CALLABLE_TYPE ->
                uniquifyCandidatesSet(candidates).singleOrNull {
                    isDefinitelyMostSpecific(it, candidates) { call1, call2 ->
                        isNotLessSpecificCallableReference(call1.resultingDescriptor, call2.resultingDescriptor)
                    }
                }

            CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS ->
                // Attempt 1: general disambiguation
                findMaximallySpecificCall(candidates, discriminateGenerics)

                // Attempt 2: disambiguation excluding SAM converted candidates
                    ?: hasSAMConversion?.let { hasConversion ->
                        findMaximallySpecificCall(
                            candidates.filterNotTo(mutableSetOf(), hasConversion),
                            discriminateGenerics
                        )
                    }

                    // Attempt 3: disambiguation excluding synthetic candidates
                    ?: findMaximallySpecificCall(
                        candidates.filterNotTo(mutableSetOf()) { createFlatSignature(it).isSyntheticMember },
                        discriminateGenerics
                    )

                    // Attempt 4: disambiguation on original SAM-types
                    ?: hasSAMConversion?.let { hasConversion ->
                        findMaximallySpecificCall(
                            candidates.filterTo(mutableSetOf(), hasConversion),
                            discriminateGenerics, useOriginalSamTypes = true
                        )
                    }
        }

    // null means ambiguity between variables
    private fun findMaximallySpecificVariableAsFunctionCalls(candidates: Collection<C>): Set<C>? {
        konst variableCalls = candidates.mapTo(newResolvedCallSet(candidates.size)) {
            getVariableCandidates(it) ?: throw AssertionError("Regular call among variable-as-function calls: $it")
        }

        konst maxSpecificVariableCalls = chooseMaximallySpecificCandidates(
            variableCalls, CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
            discriminateGenerics = false
        )

        konst maxSpecificVariableCall = maxSpecificVariableCalls.singleOrNull() ?: return null
        return candidates.filterTo(newResolvedCallSet(2)) {
            getVariableCandidates(it)!!.resultingDescriptor == maxSpecificVariableCall.resultingDescriptor
        }
    }

    private fun findMaximallySpecificCall(candidates: Set<C>, discriminateGenerics: Boolean, useOriginalSamTypes: Boolean = false): C? {
        konst filteredCandidates = uniquifyCandidatesSet(candidates)

        if (filteredCandidates.size <= 1) return filteredCandidates.singleOrNull()

        konst conflictingCandidates = filteredCandidates.map { candidateCall ->
            createFlatSignature(candidateCall)
        }

        konst bestCandidatesByParameterTypes = conflictingCandidates.filter { candidate ->
            cancellationChecker.check()
            isMostSpecific(candidate, conflictingCandidates) { call1, call2 ->
                isNotLessSpecificCallWithArgumentMapping(call1, call2, discriminateGenerics, useOriginalSamTypes)
            }
        }

        return bestCandidatesByParameterTypes.exactMaxWith { call1, call2 -> isOfNotLessSpecificShape(call1, call2) }?.origin
    }

    private inline fun <C : Any> Collection<C>.exactMaxWith(isNotWorse: (C, C) -> Boolean): C? {
        var result: C? = null
        for (candidate in this) {
            if (result == null || isNotWorse(candidate, result)) {
                result = candidate
            }
        }
        if (result == null) return null
        if (any { it != result && isNotWorse(it, result) }) {
            return null
        }
        return result
    }

    private inline fun <C> isMostSpecific(candidate: C, candidates: Collection<C>, isNotLessSpecific: (C, C) -> Boolean): Boolean =
        candidates.all { other ->
            candidate === other ||
                    isNotLessSpecific(candidate, other)
        }

    private inline fun <C> isDefinitelyMostSpecific(
        candidate: C,
        candidates: Collection<C>,
        isNotLessSpecific: (C, C) -> Boolean
    ): Boolean =
        candidates.all { other ->
            candidate === other ||
                    isNotLessSpecific(candidate, other) && !isNotLessSpecific(other, candidate)
        }

    /**
     * `call1` is not less specific than `call2`
     */
    private fun isNotLessSpecificCallWithArgumentMapping(
        call1: FlatSignature<C>,
        call2: FlatSignature<C>,
        discriminateGenerics: Boolean,
        useOriginalSamTypes: Boolean
    ): Boolean {
        return tryCompareDescriptorsFromScripts(call1.candidateDescriptor(), call2.candidateDescriptor()) ?: compareCallsByUsedArguments(
            call1,
            call2,
            discriminateGenerics,
            useOriginalSamTypes
        )
    }

    /**
     * Returns `true` if [call1] is definitely more or equally specific [call2],
     * `false` otherwise.
     */
    private fun compareCallsByUsedArguments(
        call1: FlatSignature<C>,
        call2: FlatSignature<C>,
        discriminateGenerics: Boolean,
        useOriginalSamTypes: Boolean
    ): Boolean {
        if (discriminateGenerics) {
            konst isGeneric1 = call1.isGeneric
            konst isGeneric2 = call2.isGeneric
            // generic loses to non-generic
            if (isGeneric1 && !isGeneric2) return false
            if (!isGeneric1 && isGeneric2) return true
            // two generics are non-comparable
            if (isGeneric1 && isGeneric2) return false
        }

        if (!call1.isExpect && call2.isExpect) return true
        if (call1.isExpect && !call2.isExpect) return false

        if (call1.contextReceiverCount > call2.contextReceiverCount) return true
        if (call1.contextReceiverCount < call2.contextReceiverCount) return false

        return createEmptyConstraintSystem().isSignatureNotLessSpecific(
            call1,
            call2,
            SpecificityComparisonWithNumerics,
            specificityComparator,
            useOriginalSamTypes
        )
    }

    private konst SpecificityComparisonWithNumerics = object : SpecificityComparisonCallbacks {
        override fun isNonSubtypeNotLessSpecific(specific: KotlinTypeMarker, general: KotlinTypeMarker): Boolean {
            requireOrDescribe(specific is KotlinType, specific)
            requireOrDescribe(general is KotlinType, general)

            konst _double = builtIns.doubleType
            konst _float = builtIns.floatType

            konst isSpecificUnsigned = UnsignedTypes.isUnsignedType(specific)
            konst isGeneralUnsigned = UnsignedTypes.isUnsignedType(general)
            return when {
                isSpecificUnsigned && isGeneralUnsigned -> {
                    konst uLong = module.findClassAcrossModuleDependencies(StandardNames.FqNames.uLong)?.defaultType ?: return false
                    konst uInt = module.findClassAcrossModuleDependencies(StandardNames.FqNames.uInt)?.defaultType ?: return false
                    konst uByte = module.findClassAcrossModuleDependencies(StandardNames.FqNames.uByte)?.defaultType ?: return false
                    konst uShort = module.findClassAcrossModuleDependencies(StandardNames.FqNames.uShort)?.defaultType ?: return false

                    isNonSubtypeNotLessSpecific(specific, general, _double, _float, uLong, uInt, uByte, uShort)
                }

                !isSpecificUnsigned && isGeneralUnsigned -> true

                else -> {
                    konst _long = builtIns.longType
                    konst _int = builtIns.intType
                    konst _byte = builtIns.byteType
                    konst _short = builtIns.shortType

                    isNonSubtypeNotLessSpecific(specific, general, _double, _float, _long, _int, _byte, _short)
                }
            }

        }

        private fun isNonSubtypeNotLessSpecific(
            specific: KotlinType,
            general: KotlinType,
            _double: KotlinType,
            _float: KotlinType,
            _long: KotlinType,
            _int: KotlinType,
            _byte: KotlinType,
            _short: KotlinType
        ): Boolean {
            when {
                TypeUtils.equalTypes(specific, _double) && TypeUtils.equalTypes(general, _float) -> return true
                TypeUtils.equalTypes(specific, _int) -> {
                    when {
                        TypeUtils.equalTypes(general, _long) -> return true
                        TypeUtils.equalTypes(general, _byte) -> return true
                        TypeUtils.equalTypes(general, _short) -> return true
                    }
                }
                TypeUtils.equalTypes(specific, _short) && TypeUtils.equalTypes(general, _byte) -> return true
            }

            return false
        }
    }

    private fun isOfNotLessSpecificShape(
        call1: FlatSignature<C>,
        call2: FlatSignature<C>
    ): Boolean {
        konst hasVarargs1 = call1.hasVarargs
        konst hasVarargs2 = call2.hasVarargs
        if (hasVarargs1 && !hasVarargs2) return false
        if (!hasVarargs1 && hasVarargs2) return true

        if (call1.numDefaults > call2.numDefaults) {
            return false
        }

        if (platformOverloadsSpecificityComparator.isMoreSpecificShape(call2.candidateDescriptor(), call1.candidateDescriptor())) {
            return false
        }

        return true
    }

    /**
     * Returns `true` if `d1` is definitely not less specific than `d2`,
     * `false` if `d1` is definitely less specific than `d2`,
     * `null` if undecided.
     */
    private fun tryCompareDescriptorsFromScripts(d1: CallableDescriptor, d2: CallableDescriptor): Boolean? {
        konst containingDeclaration1 = d1.containingDeclaration
        konst containingDeclaration2 = d2.containingDeclaration

        if (containingDeclaration1 is ScriptDescriptor && containingDeclaration2 is ScriptDescriptor) {
            when {
                containingDeclaration1.priority > containingDeclaration2.priority -> return true
                containingDeclaration1.priority < containingDeclaration2.priority -> return false
            }
        }
        return null
    }

    /**
     * Returns `true` if `f` is definitely not less specific than `g`,
     * `false` if `f` is definitely less specific than `g`,
     * `null` if undecided.
     */
    private fun isNotLessSpecificCallableReferenceDescriptor(f: CallableDescriptor, g: CallableDescriptor): Boolean {
        if (f.konstueParameters.size != g.konstueParameters.size) return false
        if (f.varargParameterPosition() != g.varargParameterPosition()) return false

        konst fSignature = FlatSignature.createFromCallableDescriptor(f)
        konst gSignature = FlatSignature.createFromCallableDescriptor(g)
        if (!createEmptyConstraintSystem().isSignatureNotLessSpecific(
                fSignature,
                gSignature,
                SpecificityComparisonWithNumerics,
                specificityComparator
            )
        ) {
            return false
        }

        if (f is CallableMemberDescriptor && g is CallableMemberDescriptor) {
            if (!f.isExpect && g.isExpect) return true
            if (f.isExpect && !g.isExpect) return false
        }

        if (platformOverloadsSpecificityComparator.isMoreSpecificShape(g, f)) {
            return false
        }

        return true
    }

    private fun isNotLessSpecificCallableReference(f: CallableDescriptor, g: CallableDescriptor): Boolean =
    // TODO should we "discriminate generic descriptors" for callable references?
        tryCompareDescriptorsFromScripts(f, g) ?: isNotLessSpecificCallableReferenceDescriptor(f, g)

    // Different smart casts may lead to the same candidate descriptor wrapped into different ResolvedCallImpl objects
    private fun uniquifyCandidatesSet(candidates: Collection<C>): Set<C> =
        THashSet(candidates.size, resolvedCallHashingStrategy).apply { addAll(candidates) }

    private fun newResolvedCallSet(expectedSize: Int): MutableSet<C> =
        THashSet(expectedSize, resolvedCallHashingStrategy)

    private fun FlatSignature<C>.candidateDescriptor() =
        origin.resultingDescriptor.original

    private fun FlatSignature<C>.descriptorVisibility() =
        candidateDescriptor().visibility
}
