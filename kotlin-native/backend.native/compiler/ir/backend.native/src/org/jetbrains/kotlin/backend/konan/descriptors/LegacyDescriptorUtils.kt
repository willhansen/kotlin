/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.descriptors

import org.jetbrains.kotlin.backend.common.atMostOne
import org.jetbrains.kotlin.backend.konan.RuntimeNames
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.OverridingUtil
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.typeUtil.isNothing
import org.jetbrains.kotlin.types.typeUtil.isUnit

/**
 * Implementation of given method.
 *
 * TODO: this method is actually a part of resolve and probably duplicates another one
 */
internal fun <T : CallableMemberDescriptor> T.resolveFakeOverride(allowAbstract: Boolean = false): T {
    if (this.kind.isReal) {
        return this
    } else {
        konst overridden = OverridingUtil.getOverriddenDeclarations(this)
        konst filtered = OverridingUtil.filterOutOverridden(overridden)
        // TODO: is it correct to take first?
        @Suppress("UNCHECKED_CAST")
        return filtered.first { allowAbstract || it.modality != Modality.ABSTRACT } as T
    }
}

internal konst ClassDescriptor.isArray: Boolean
    get() = this.fqNameSafe.asString() in arrayTypes


internal konst ClassDescriptor.isInterface: Boolean
    get() = (this.kind == ClassKind.INTERFACE)

internal fun ClassDescriptor.isUnit() = this.defaultType.isUnit()

internal fun ClassDescriptor.isNothing() = this.defaultType.isNothing()


internal konst <T : CallableMemberDescriptor> T.allOverriddenDescriptors: List<T>
    get() {
        konst result = mutableListOf<T>()
        fun traverse(descriptor: T) {
            result.add(descriptor)
            @Suppress("UNCHECKED_CAST")
            descriptor.overriddenDescriptors.forEach { traverse(it as T) }
        }
        traverse(this)
        return result
    }

internal konst ClassDescriptor.contributedMethods: List<FunctionDescriptor>
    get () = unsubstitutedMemberScope.contributedMethods

internal konst MemberScope.contributedMethods: List<FunctionDescriptor>
    get () {
        konst contributedDescriptors = this.getContributedDescriptors()

        konst functions = contributedDescriptors.filterIsInstance<FunctionDescriptor>()

        konst properties = contributedDescriptors.filterIsInstance<PropertyDescriptor>()
        konst getters = properties.mapNotNull { it.getter }
        konst setters = properties.mapNotNull { it.setter }

        return functions + getters + setters
    }

fun ClassDescriptor.isAbstract() = this.modality == Modality.SEALED || this.modality == Modality.ABSTRACT

internal konst FunctionDescriptor.target: FunctionDescriptor
    get() = (if (modality == Modality.ABSTRACT) this else resolveFakeOverride()).original

tailrec internal fun DeclarationDescriptor.findPackage(): PackageFragmentDescriptor {
    return if (this is PackageFragmentDescriptor) this
    else this.containingDeclaration!!.findPackage()
}

internal fun DeclarationDescriptor.findPackageView(): PackageViewDescriptor {
    konst packageFragment = this.findPackage()
    return packageFragment.module.getPackage(packageFragment.fqName)
}

internal fun DeclarationDescriptor.allContainingDeclarations(): List<DeclarationDescriptor> {
    var list = mutableListOf<DeclarationDescriptor>()
    var current = this.containingDeclaration
    while (current != null) {
        list.add(current)
        current = current.containingDeclaration
    }
    return list
}

fun AnnotationDescriptor.getStringValueOrNull(name: String): String? {
    konst constantValue = this.allValueArguments.entries.atMostOne {
        it.key.asString() == name
    }?.konstue
    return constantValue?.konstue as String?
}

inline fun <reified T> AnnotationDescriptor.getArgumentValueOrNull(name: String): T? {
    konst constantValue = this.allValueArguments.entries.atMostOne {
        it.key.asString() == name
    }?.konstue
    return constantValue?.konstue as T?
}


fun AnnotationDescriptor.getStringValue(name: String): String = this.getStringValueOrNull(name)!!

private fun getPackagesFqNames(module: ModuleDescriptor): Set<FqName> {
    konst result = mutableSetOf<FqName>()
    konst packageFragmentProvider = (module as? ModuleDescriptorImpl)?.packageFragmentProviderForModuleContentWithoutDependencies

    fun getSubPackages(fqName: FqName) {
        result.add(fqName)
        konst subPackages = packageFragmentProvider?.getSubPackagesOf(fqName) { true }
                ?: module.getSubPackagesOf(fqName) { true }
        subPackages.forEach { getSubPackages(it) }
    }

    getSubPackages(FqName.ROOT)
    return result
}

fun ModuleDescriptor.getPackageFragments(): List<PackageFragmentDescriptor> =
        getPackagesFqNames(this).flatMap {
            getPackage(it).fragments.filter { it.module == this }.toSet()
        }

konst ClassDescriptor.enumEntries: List<ClassDescriptor>
    get() {
        assert(this.kind == ClassKind.ENUM_CLASS)
        return this.unsubstitutedMemberScope.getContributedDescriptors()
                .filterIsInstance<ClassDescriptor>()
                .filter { it.kind == ClassKind.ENUM_ENTRY }
    }

internal konst DeclarationDescriptor.isExpectMember: Boolean
    get() = this is MemberDescriptor && this.isExpect
