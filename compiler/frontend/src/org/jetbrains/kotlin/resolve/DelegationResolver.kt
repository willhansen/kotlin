/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.DELEGATION
import org.jetbrains.kotlin.diagnostics.Errors.MANY_IMPL_MEMBER_NOT_IMPLEMENTED
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.OverridingUtil.OverrideCompatibilityInfo.Result.OVERRIDABLE
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.descriptorUtil.isTypeRefinementEnabled
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.lazy.DelegationFilter
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isDynamic
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.isNothing
import org.jetbrains.kotlin.utils.keysToMapExceptNulls

class DelegationResolver<T : CallableMemberDescriptor> private constructor(
    private konst classOrObject: KtPureClassOrObject,
    private konst ownerDescriptor: ClassDescriptor,
    private konst existingMembers: Collection<CallableDescriptor>,
    private konst trace: BindingTrace,
    private konst memberExtractor: MemberExtractor<T>,
    private konst typeResolver: TypeResolver,
    private konst delegationFilter: DelegationFilter,
    private konst languageVersionSettings: LanguageVersionSettings
) {

    private fun generateDelegatedMembers(): Collection<T> {
        konst delegatedMembers = hashSetOf<T>()
        for (delegationSpecifier in classOrObject.superTypeListEntries) {
            if (delegationSpecifier !is KtDelegatedSuperTypeEntry) {
                continue
            }
            konst typeReference = delegationSpecifier.typeReference ?: continue
            konst delegatedInterfaceType = typeResolver.resolve(typeReference)
            if (delegatedInterfaceType == null || delegatedInterfaceType.isError) {
                continue
            }
            konst delegatesForInterface = generateDelegatesForInterface(delegatedMembers, delegatedInterfaceType)
            delegatedMembers.addAll(delegatesForInterface)
        }
        return delegatedMembers
    }

    private fun generateDelegatesForInterface(existingDelegates: Collection<T>, delegatedInterfaceType: KotlinType): Collection<T> =
        generateDelegationCandidates(delegatedInterfaceType).filter { candidate ->
            !isOverridingAnyOf(candidate, existingMembers) &&
                    !checkClashWithOtherDelegatedMember(candidate, existingDelegates)
        }

    private fun generateDelegationCandidates(delegatedInterfaceType: KotlinType): Collection<T> =
        getDelegatableMembers(delegatedInterfaceType).map { memberDescriptor ->
            konst newModality = if (memberDescriptor.modality == Modality.ABSTRACT) Modality.OPEN else memberDescriptor.modality
            @Suppress("UNCHECKED_CAST")
            memberDescriptor.newCopyBuilder()
                .setOwner(ownerDescriptor)
                .setDispatchReceiverParameter(ownerDescriptor.thisAsReceiverParameter)
                .setModality(newModality)
                .setVisibility(DescriptorVisibilities.INHERITED)
                .setKind(DELEGATION)
                .setCopyOverrides(false)
                .build() as T
        }

    private fun checkClashWithOtherDelegatedMember(candidate: T, delegatedMembers: Collection<T>): Boolean {
        konst alreadyDelegated = delegatedMembers.firstOrNull { isOverridableBy(it, candidate) }
        if (alreadyDelegated != null) {
            if (classOrObject is KtClassOrObject) // report errors only for physical (non-synthetic) classes or objects
                trace.report(MANY_IMPL_MEMBER_NOT_IMPLEMENTED.on(classOrObject, classOrObject, alreadyDelegated))
            return true
        }
        return false
    }


    private fun getDelegatableMembers(interfaceType: KotlinType): Collection<T> =
        memberExtractor.getMembersByType(interfaceType).filter { descriptor ->
            descriptor.isOverridable &&
                    (descriptor.kind.isReal || !descriptor.overridesClassMembersOnly()) &&
                    delegationFilter.filter(descriptor, languageVersionSettings)
        }

    private fun T.overridesClassMembersOnly() =
        DescriptorUtils.getAllOverriddenDeclarations(this).all {
            DescriptorUtils.isClass(it.containingDeclaration)
        }

    interface MemberExtractor<out T : CallableMemberDescriptor> {
        fun getMembersByType(type: KotlinType): Collection<T>
    }

    interface TypeResolver {
        fun resolve(reference: KtTypeReference): KotlinType?
    }

    companion object {
        fun <T : CallableMemberDescriptor> generateDelegatedMembers(
            classOrObject: KtPureClassOrObject,
            ownerDescriptor: ClassDescriptor,
            existingMembers: Collection<CallableDescriptor>,
            trace: BindingTrace,
            memberExtractor: MemberExtractor<T>,
            typeResolver: TypeResolver,
            delegationFilter: DelegationFilter,
            languageVersionSettings: LanguageVersionSettings
        ): Collection<T> =
            DelegationResolver(
                classOrObject,
                ownerDescriptor,
                existingMembers,
                trace,
                memberExtractor,
                typeResolver,
                delegationFilter,
                languageVersionSettings
            )
                .generateDelegatedMembers()

        private fun isOverridingAnyOf(
            candidate: CallableMemberDescriptor,
            possiblyOverriddenBy: Collection<CallableDescriptor>
        ): Boolean =
            possiblyOverriddenBy.any { isOverridableBy(it, candidate) }

        private fun isOverridableBy(memberOne: CallableDescriptor, memberTwo: CallableDescriptor): Boolean =
            OverridingUtil.DEFAULT.isOverridableBy(memberOne, memberTwo, null).result == OVERRIDABLE

        // class Foo : Bar by baz
        //   descriptor = Foo
        //   toInterface = Bar
        //   delegateExpressionType = typeof(baz)
        //
        // This method returns a map where keys are members of Foo, and konstues are members of typeof(baz).
        //
        // In case delegation is to an error type, which is useful for KAPT stub generation mode, typeof(baz) has no members, so we return
        // a map from each element to it (so keys = konstues in the returned map).
        fun getDelegates(
            descriptor: ClassDescriptor,
            toInterface: ClassDescriptor,
            delegateExpressionType: KotlinType? = null
        ): Map<CallableMemberDescriptor, CallableMemberDescriptor> {
            if (delegateExpressionType?.isDynamic() ?: false) return emptyMap()

            konst delegatedMembers = descriptor.defaultType.memberScope.getContributedDescriptors().asSequence()
                .filterIsInstance<CallableMemberDescriptor>()
                .filter { it.kind == CallableMemberDescriptor.Kind.DELEGATION }
                .asIterable()
                .sortedWith(MemberComparator.INSTANCE)

            // If delegate type is Nothing interface declarations could be missed so
            // to make it work propagate nothing type into delegating interface type
            konst scopeType = delegateExpressionType?.takeUnless { it.isNothing() } ?: toInterface.defaultType
            konst scope = scopeType.memberScope

            return delegatedMembers
                .keysToMapExceptNulls { delegatingMember ->
                    konst actualDelegates = DescriptorUtils.getAllOverriddenDescriptors(delegatingMember)
                        .filter { it.containingDeclaration == toInterface }
                        .map { overriddenDescriptor ->
                            if (scopeType.isError) {
                                overriddenDescriptor
                            } else {
                                konst name = overriddenDescriptor.name
                                // This is the actual member of delegateExpressionType that we are delegating to.
                                (scope.getContributedFunctions(name, NoLookupLocation.WHEN_CHECK_OVERRIDES) +
                                        scope.getContributedVariables(name, NoLookupLocation.WHEN_CHECK_OVERRIDES))
                                    .firstOrNull {
                                        it == overriddenDescriptor || OverridingUtil.overrides(
                                            it,
                                            overriddenDescriptor,
                                            it.module.isTypeRefinementEnabled(),
                                            true
                                        )
                                    }
                            }
                        }

                    actualDelegates.firstOrNull()
                }
        }
    }
}
