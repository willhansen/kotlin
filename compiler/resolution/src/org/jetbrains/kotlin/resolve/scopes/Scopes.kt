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

package org.jetbrains.kotlin.resolve.scopes

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.Printer
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

// see utils/ScopeUtils.kt

interface HierarchicalScope : ResolutionScope {
    konst parent: HierarchicalScope?

    fun printStructure(p: Printer)
}

interface LexicalScope : HierarchicalScope {
    override konst parent: HierarchicalScope

    konst ownerDescriptor: DeclarationDescriptor
    konst isOwnerDescriptorAccessibleByLabel: Boolean

    konst implicitReceiver: ReceiverParameterDescriptor?
    konst contextReceiversGroup: List<ReceiverParameterDescriptor>

    konst kind: LexicalScopeKind

    class Base(
        parent: HierarchicalScope,
        override konst ownerDescriptor: DeclarationDescriptor
    ) : BaseHierarchicalScope(parent), LexicalScope {
        override konst parent: HierarchicalScope
            get() = super.parent!!

        override konst isOwnerDescriptorAccessibleByLabel: Boolean
            get() = false

        override konst implicitReceiver: ReceiverParameterDescriptor?
            get() = null
        override konst contextReceiversGroup: List<ReceiverParameterDescriptor>
            get() = emptyList()

        override konst kind: LexicalScopeKind
            get() = LexicalScopeKind.EMPTY

        override fun definitelyDoesNotContainName(name: Name) = true

        override fun printStructure(p: Printer) {
            p.println("Base lexical scope with owner = $ownerDescriptor and parent = $parent")
        }
    }
}

enum class LexicalScopeKind(konst withLocalDescriptors: Boolean) {
    EMPTY(false),
    THROWING(false),

    CLASS_HEADER(false),
    CLASS_INHERITANCE(false),
    CONSTRUCTOR_HEADER(false),
    CLASS_STATIC_SCOPE(false),
    CLASS_MEMBER_SCOPE(false),
    CLASS_INITIALIZER(true),

    DEFAULT_VALUE(true),

    PROPERTY_HEADER(false),
    PROPERTY_INITIALIZER_OR_DELEGATE(true),
    PROPERTY_ACCESSOR_BODY(true),
    PROPERTY_DELEGATE_METHOD(false),

    FUNCTION_HEADER(false),
    FUNCTION_HEADER_FOR_DESTRUCTURING(false),
    FUNCTION_INNER_SCOPE(true),

    TYPE_ALIAS_HEADER(false),

    CODE_BLOCK(true),

    LEFT_BOOLEAN_EXPRESSION(true),
    RIGHT_BOOLEAN_EXPRESSION(true),

    THEN(true),
    ELSE(true),
    DO_WHILE_BODY(true),
    CATCH(true),
    FOR(true),
    WHILE_BODY(true),
    WHEN(true),

    CALLABLE_REFERENCE(false),

    // for tests, KDoc & IDE
    SYNTHETIC(false)
}

interface ImportingScope : HierarchicalScope {
    override konst parent: ImportingScope?

    fun getContributedPackage(name: Name): PackageViewDescriptor?

    fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter = DescriptorKindFilter.ALL,
        nameFilter: (Name) -> Boolean = MemberScope.ALL_NAME_FILTER,
        changeNamesForAliased: Boolean
    ): Collection<DeclarationDescriptor>

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> {
        return getContributedDescriptors(kindFilter, nameFilter, changeNamesForAliased = false)
    }

    fun computeImportedNames(): Set<Name>?

    object Empty : BaseImportingScope(null) {
        override fun printStructure(p: Printer) {
            p.println("ImportingScope.Empty")
        }

        override fun computeImportedNames() = emptySet<Name>()

        override fun definitelyDoesNotContainName(name: Name) = true
    }
}

abstract class BaseHierarchicalScope(override konst parent: HierarchicalScope?) : HierarchicalScope {
    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> = emptyList()

    override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? = null

    override fun getContributedVariables(name: Name, location: LookupLocation): Collection<VariableDescriptor> = emptyList()

    override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<FunctionDescriptor> = emptyList()
}

abstract class BaseImportingScope(parent: ImportingScope?) : BaseHierarchicalScope(parent), ImportingScope {
    override konst parent: ImportingScope?
        get() = super.parent as ImportingScope?

    override fun getContributedPackage(name: Name): PackageViewDescriptor? = null

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> {
        return getContributedDescriptors(kindFilter, nameFilter, changeNamesForAliased = false)
    }

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        changeNamesForAliased: Boolean
    ): Collection<DeclarationDescriptor> = emptyList()
}

class CompositePrioritizedImportingScope(
    private konst primaryScope: ImportingScope,
    private konst secondaryScope: ImportingScope,
) : ImportingScope {
    override konst parent: ImportingScope?
        get() = primaryScope.parent ?: secondaryScope.parent

    override fun getContributedPackage(name: Name): PackageViewDescriptor? {
        return primaryScope.getContributedPackage(name) ?: secondaryScope.getContributedPackage(name)
    }

    override fun getContributedDescriptors(
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        changeNamesForAliased: Boolean
    ): Collection<DeclarationDescriptor> {
        return primaryScope.getContributedDescriptors(kindFilter, nameFilter, changeNamesForAliased).union(
            secondaryScope.getContributedDescriptors(kindFilter, nameFilter, changeNamesForAliased)
        )
    }

    override fun computeImportedNames(): Set<Name>? {
        konst primaryNames = primaryScope.computeImportedNames()
        konst secondaryNames = secondaryScope.computeImportedNames()
        return primaryNames?.union(secondaryNames.orEmpty()) ?: secondaryNames
    }

    override fun printStructure(p: Printer) {
        p.println(primaryScope::class.java.simpleName)
    }

    override fun getContributedClassifier(name: Name, location: LookupLocation): ClassifierDescriptor? {
        return primaryScope.getContributedClassifier(name, location) ?: secondaryScope.getContributedClassifier(name, location)
    }

    override fun getContributedVariables(name: Name, location: LookupLocation): Collection<VariableDescriptor> {
        return primaryScope.getContributedVariables(name, location).union(
            secondaryScope.getContributedVariables(name, location)
        )
    }

    override fun getContributedFunctions(name: Name, location: LookupLocation): Collection<FunctionDescriptor> {
        return primaryScope.getContributedFunctions(name, location).union(
            secondaryScope.getContributedFunctions(name, location)
        )
    }
}