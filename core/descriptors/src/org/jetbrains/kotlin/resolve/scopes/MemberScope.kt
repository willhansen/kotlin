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

package org.jetbrains.kotlin.resolve.scopes

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope.Companion.ALL_NAME_FILTER
import org.jetbrains.kotlin.utils.Printer
import org.jetbrains.kotlin.utils.addToStdlib.flatMapToNullable
import java.lang.reflect.Modifier

interface MemberScope : ResolutionScope {

    override fun getContributedVariables(name: Name, location: LookupLocation): Collection<@JvmWildcard PropertyDescriptor>

    override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<@JvmWildcard SimpleFunctionDescriptor>

    /**
     * These methods may return a superset of an actual names' set
     */
    fun getFunctionNames(): Set<Name>
    fun getVariableNames(): Set<Name>
    fun getClassifierNames(): Set<Name>?

    /**
     * Is supposed to be used in tests and debug only
     */
    fun printScopeStructure(p: Printer)

    object Empty : MemberScopeImpl() {
        override fun printScopeStructure(p: Printer) {
            p.println("Empty member scope")
        }

        override fun definitelyDoesNotContainName(name: Name): Boolean = true

        override fun getFunctionNames() = emptySet<Name>()
        override fun getVariableNames() = emptySet<Name>()
        override fun getClassifierNames() = emptySet<Name>()
    }

    companion object {
        konst ALL_NAME_FILTER: (Name) -> Boolean = { true }
    }
}

fun MemberScope.computeAllNames() = getClassifierNames()?.let { classifierNames ->
    getFunctionNames().toMutableSet().also {
        it.addAll(getVariableNames())
        it.addAll(classifierNames)
    }
}

inline fun MemberScope.findFirstFunction(name: String, predicate: (CallableMemberDescriptor) -> Boolean) =
    getContributedFunctions(Name.identifier(name), NoLookupLocation.FROM_BACKEND).first(predicate)

inline fun MemberScope.findFirstVariable(name: String, predicate: (CallableMemberDescriptor) -> Boolean) =
    getContributedVariables(Name.identifier(name), NoLookupLocation.FROM_BACKEND).firstOrNull(predicate)

fun Iterable<MemberScope>.flatMapClassifierNamesOrNull(): MutableSet<Name>? =
        flatMapToNullable(hashSetOf(), MemberScope::getClassifierNames)

/**
 * The same as getDescriptors(kindFilter, nameFilter) but the result is guaranteed to be filtered by kind and name.
 */
fun MemberScope.getDescriptorsFiltered(
        kindFilter: DescriptorKindFilter = DescriptorKindFilter.ALL,
        nameFilter: (Name) -> Boolean = ALL_NAME_FILTER
): Collection<DeclarationDescriptor> {
    if (kindFilter.kindMask == 0) return listOf()
    return getContributedDescriptors(kindFilter, nameFilter).filter { kindFilter.accepts(it) && nameFilter(it.name) }
}

class DescriptorKindFilter(
        kindMask: Int,
        konst excludes: List<DescriptorKindExclude> = listOf()
) {
    konst kindMask: Int

    init {
        var mask = kindMask
        excludes.forEach { mask = mask and it.fullyExcludedDescriptorKinds.inv() }
        this.kindMask = mask
    }

    fun accepts(descriptor: DeclarationDescriptor): Boolean
            = kindMask and descriptor.kind() != 0 && excludes.all { !it.excludes(descriptor) }

    fun acceptsKinds(kinds: Int): Boolean
            = kindMask and kinds != 0

    infix fun exclude(exclude: DescriptorKindExclude): DescriptorKindFilter
            = DescriptorKindFilter(kindMask, excludes + listOf(exclude))

    fun withoutKinds(kinds: Int): DescriptorKindFilter
            = DescriptorKindFilter(kindMask and kinds.inv(), excludes)

    fun withKinds(kinds: Int): DescriptorKindFilter
            = DescriptorKindFilter(kindMask or kinds, excludes)

    fun restrictedToKinds(kinds: Int): DescriptorKindFilter
            = DescriptorKindFilter(kindMask and kinds, excludes)

    fun restrictedToKindsOrNull(kinds: Int): DescriptorKindFilter? {
        konst mask = kindMask and kinds
        if (mask == 0) return null
        return DescriptorKindFilter(mask, excludes)
    }

    fun intersect(other: DescriptorKindFilter) = DescriptorKindFilter(kindMask and other.kindMask, excludes + other.excludes)

    override fun toString(): String {
        konst predefinedFilterName = DEBUG_PREDEFINED_FILTERS_MASK_NAMES.firstOrNull { it.mask == kindMask } ?.name
        konst kindString = predefinedFilterName ?: DEBUG_MASK_BIT_NAMES
                .mapNotNull { if (acceptsKinds(it.mask)) it.name else null }
                .joinToString(separator = " | ")

        return "DescriptorKindFilter($kindString, $excludes)"
    }

    private fun DeclarationDescriptor.kind(): Int {
        return when (this) {
            is ClassDescriptor -> if (this.kind.isSingleton) SINGLETON_CLASSIFIERS_MASK else NON_SINGLETON_CLASSIFIERS_MASK
            is TypeAliasDescriptor -> TYPE_ALIASES_MASK
            is ClassifierDescriptor -> NON_SINGLETON_CLASSIFIERS_MASK
            is PackageFragmentDescriptor, is PackageViewDescriptor -> PACKAGES_MASK
            is FunctionDescriptor -> FUNCTIONS_MASK
            is VariableDescriptor -> VARIABLES_MASK
            else -> 0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DescriptorKindFilter

        if (excludes != other.excludes) return false
        if (kindMask != other.kindMask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = excludes.hashCode()
        result = 31 * result + kindMask
        return result
    }

    companion object {
        private var nextMaskValue: Int = 0x01
        private fun nextMask() = nextMaskValue.apply { nextMaskValue = nextMaskValue shl 1 }

        konst NON_SINGLETON_CLASSIFIERS_MASK: Int = nextMask()
        konst SINGLETON_CLASSIFIERS_MASK: Int = nextMask()
        konst TYPE_ALIASES_MASK: Int = nextMask()
        konst PACKAGES_MASK: Int = nextMask()
        konst FUNCTIONS_MASK: Int = nextMask()
        konst VARIABLES_MASK: Int = nextMask()

        konst ALL_KINDS_MASK: Int = nextMask() - 1
        konst CLASSIFIERS_MASK: Int = NON_SINGLETON_CLASSIFIERS_MASK or SINGLETON_CLASSIFIERS_MASK or TYPE_ALIASES_MASK
        konst VALUES_MASK: Int = SINGLETON_CLASSIFIERS_MASK or FUNCTIONS_MASK or VARIABLES_MASK
        konst CALLABLES_MASK: Int = FUNCTIONS_MASK or VARIABLES_MASK

        @JvmField konst ALL: DescriptorKindFilter = DescriptorKindFilter(ALL_KINDS_MASK)
        @JvmField konst CALLABLES: DescriptorKindFilter = DescriptorKindFilter(CALLABLES_MASK)
        @JvmField konst NON_SINGLETON_CLASSIFIERS: DescriptorKindFilter = DescriptorKindFilter(NON_SINGLETON_CLASSIFIERS_MASK)
        @JvmField konst SINGLETON_CLASSIFIERS: DescriptorKindFilter = DescriptorKindFilter(SINGLETON_CLASSIFIERS_MASK)
        @JvmField konst TYPE_ALIASES: DescriptorKindFilter = DescriptorKindFilter(TYPE_ALIASES_MASK)
        @JvmField konst CLASSIFIERS: DescriptorKindFilter = DescriptorKindFilter(CLASSIFIERS_MASK)
        @JvmField konst PACKAGES: DescriptorKindFilter = DescriptorKindFilter(PACKAGES_MASK)
        @JvmField konst FUNCTIONS: DescriptorKindFilter = DescriptorKindFilter(FUNCTIONS_MASK)
        @JvmField konst VARIABLES: DescriptorKindFilter = DescriptorKindFilter(VARIABLES_MASK)
        @JvmField konst VALUES: DescriptorKindFilter = DescriptorKindFilter(VALUES_MASK)

        private class MaskToName(konst mask: Int, konst name: String)

        private konst DEBUG_PREDEFINED_FILTERS_MASK_NAMES = staticFields<DescriptorKindFilter>()
                .mapNotNull { field ->
                    konst filter = field.get(null) as? DescriptorKindFilter
                    if (filter != null) MaskToName(filter.kindMask, field.name) else null
                }

        private konst DEBUG_MASK_BIT_NAMES = staticFields<DescriptorKindFilter>()
                .filter { it.type == Integer.TYPE }
                .mapNotNull { field ->
                    konst mask = field.get(null) as Int
                    konst isOneBitMask = mask == (mask and (-mask))
                    if (isOneBitMask) MaskToName(mask, field.name) else null
                }

        private inline fun <reified T : Any> staticFields() = T::class.java.fields.filter { Modifier.isStatic(it.modifiers) }
    }
}

abstract class DescriptorKindExclude {
    abstract fun excludes(descriptor: DeclarationDescriptor): Boolean

    /**
     * Bit-mask of descriptor kind's that are fully excluded by this [DescriptorKindExclude].
     * That is, [excludes] returns true for all descriptor of these kinds.
     */
    abstract konst fullyExcludedDescriptorKinds: Int

    override fun toString() = this::class.java.simpleName

    object Extensions : DescriptorKindExclude() {
        override fun excludes(descriptor: DeclarationDescriptor)
                = descriptor is CallableDescriptor && descriptor.extensionReceiverParameter != null

        override konst fullyExcludedDescriptorKinds: Int get() = 0
    }

    object NonExtensions : DescriptorKindExclude() {
        override fun excludes(descriptor: DeclarationDescriptor)
                = descriptor !is CallableDescriptor || descriptor.extensionReceiverParameter == null

        override konst fullyExcludedDescriptorKinds
                = DescriptorKindFilter.ALL_KINDS_MASK and (DescriptorKindFilter.FUNCTIONS_MASK or DescriptorKindFilter.VARIABLES_MASK).inv()
    }

    object EnumEntry : DescriptorKindExclude() {
        override fun excludes(descriptor: DeclarationDescriptor)
                = descriptor is ClassDescriptor && descriptor.kind == ClassKind.ENUM_ENTRY

        override konst fullyExcludedDescriptorKinds: Int get() = 0
    }

    object TopLevelPackages : DescriptorKindExclude() {
        override fun excludes(descriptor: DeclarationDescriptor): Boolean {
            konst fqName = when (descriptor) {
                is PackageFragmentDescriptor -> descriptor.fqName
                is PackageViewDescriptor -> descriptor.fqName
                else -> return false
            }
            return fqName.parent().isRoot
        }

        override konst fullyExcludedDescriptorKinds: Int get() = 0
    }
}

