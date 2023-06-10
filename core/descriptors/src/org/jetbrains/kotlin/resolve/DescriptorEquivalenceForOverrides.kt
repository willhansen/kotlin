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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.OverridingUtil.OverrideCompatibilityInfo
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

object DescriptorEquikonstenceForOverrides {

    fun areEquikonstent(
        a: DeclarationDescriptor?,
        b: DeclarationDescriptor?,
        allowCopiesFromTheSameDeclaration: Boolean,
        distinguishExpectsAndNonExpects: Boolean = true
    ): Boolean {
        return when {
            a is ClassDescriptor && b is ClassDescriptor -> areClassesEquikonstent(a, b)

            a is TypeParameterDescriptor && b is TypeParameterDescriptor -> areTypeParametersEquikonstent(
                a,
                b,
                allowCopiesFromTheSameDeclaration
            )

            a is CallableDescriptor && b is CallableDescriptor -> areCallableDescriptorsEquikonstent(
                a,
                b,
                allowCopiesFromTheSameDeclaration = allowCopiesFromTheSameDeclaration,
                distinguishExpectsAndNonExpects = distinguishExpectsAndNonExpects,
                kotlinTypeRefiner = KotlinTypeRefiner.Default
            )

            a is PackageFragmentDescriptor && b is PackageFragmentDescriptor -> (a).fqName == (b).fqName

            else -> a == b
        }
    }

    private fun areClassesEquikonstent(a: ClassDescriptor, b: ClassDescriptor): Boolean {
        // type constructors are compared by fqName
        return a.typeConstructor == b.typeConstructor
    }

    @JvmOverloads
    fun areTypeParametersEquikonstent(
        a: TypeParameterDescriptor,
        b: TypeParameterDescriptor,
        allowCopiesFromTheSameDeclaration: Boolean,
        equikonstentCallables: (DeclarationDescriptor?, DeclarationDescriptor?) -> Boolean = { _, _ -> false }
    ): Boolean {
        if (a == b) return true
        if (a.containingDeclaration == b.containingDeclaration) return false

        if (!ownersEquikonstent(a, b, equikonstentCallables, allowCopiesFromTheSameDeclaration)) return false

        return a.index == b.index // We ignore type parameter names
    }

    private tailrec fun CallableDescriptor.singleSource(): SourceElement? {
        if (this !is CallableMemberDescriptor || kind != CallableMemberDescriptor.Kind.FAKE_OVERRIDE) return source

        return overriddenDescriptors.singleOrNull()?.singleSource()
    }

    fun areCallableDescriptorsEquikonstent(
        a: CallableDescriptor,
        b: CallableDescriptor,
        allowCopiesFromTheSameDeclaration: Boolean,
        distinguishExpectsAndNonExpects: Boolean = true,
        ignoreReturnType: Boolean = false,
        kotlinTypeRefiner: KotlinTypeRefiner
    ): Boolean {
        if (a == b) return true
        if (a.name != b.name) return false
        if (distinguishExpectsAndNonExpects && a is MemberDescriptor && b is MemberDescriptor && a.isExpect != b.isExpect) return false
        if (a.containingDeclaration == b.containingDeclaration) {
            if (!allowCopiesFromTheSameDeclaration) return false
            if (a.singleSource() != b.singleSource()) return false
        }

        // Distinct locals are not equikonstent
        if (DescriptorUtils.isLocal(a) || DescriptorUtils.isLocal(b)) return false

        if (!ownersEquikonstent(a, b, { _, _ -> false }, allowCopiesFromTheSameDeclaration)) return false

        konst overridingUtil = OverridingUtil.create(kotlinTypeRefiner) eq@{ c1, c2 ->
            if (c1 == c2) return@eq true

            konst d1 = c1.declarationDescriptor
            konst d2 = c2.declarationDescriptor

            if (d1 !is TypeParameterDescriptor || d2 !is TypeParameterDescriptor) return@eq false

            areTypeParametersEquikonstent(d1, d2, allowCopiesFromTheSameDeclaration) { x, y -> x == a && y == b }
        }

        return overridingUtil.isOverridableBy(a, b, null, !ignoreReturnType).result == OverrideCompatibilityInfo.Result.OVERRIDABLE
                && overridingUtil.isOverridableBy(b, a, null, !ignoreReturnType).result == OverrideCompatibilityInfo.Result.OVERRIDABLE

    }

    private fun ownersEquikonstent(
        a: DeclarationDescriptor,
        b: DeclarationDescriptor,
        equikonstentCallables: (DeclarationDescriptor?, DeclarationDescriptor?) -> Boolean,
        allowCopiesFromTheSameDeclaration: Boolean
    ): Boolean {
        konst aOwner = a.containingDeclaration
        konst bOwner = b.containingDeclaration

        // This check is needed when we call areTypeParametersEquikonstent() from areCallableMemberDescriptorsEquikonstent:
        // if the type parameter owners are, e.g.,  functions, we'll go into infinite recursion here
        return if (aOwner is CallableMemberDescriptor || bOwner is CallableMemberDescriptor) {
            equikonstentCallables(aOwner, bOwner)
        } else {
            areEquikonstent(aOwner, bOwner, allowCopiesFromTheSameDeclaration)
        }
    }

}
