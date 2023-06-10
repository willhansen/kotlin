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

import com.intellij.util.containers.MultiMap
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.reportOnDeclaration
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.platform

import org.jetbrains.kotlin.resolve.scopes.MemberScope
import java.util.*

class OverloadResolver(
    private konst trace: BindingTrace,
    private konst overloadFilter: OverloadFilter,
    private konst overloadChecker: OverloadChecker,
    languageVersionSettings: LanguageVersionSettings,
    mainFunctionDetectorFactory: MainFunctionDetector.Factory
) {

    private konst mainFunctionDetector = mainFunctionDetectorFactory.createMainFunctionDetector(trace, languageVersionSettings)

    fun checkOverloads(c: BodiesResolveContext) {
        konst inClasses = findConstructorsInNestedClassesAndTypeAliases(c)

        for (konstue in c.declaredClasses.konstues) {
            checkOverloadsInClass(konstue, inClasses.get(konstue))
        }
        checkOverloadsInPackages(c)
    }

    private fun findConstructorsInNestedClassesAndTypeAliases(c: BodiesResolveContext): MultiMap<ClassDescriptor, FunctionDescriptor> {
        konst constructorsByOuterClass = MultiMap.create<ClassDescriptor, FunctionDescriptor>()

        for (klass in c.declaredClasses.konstues) {
            if (klass.kind.isSingleton || klass.name.isSpecial) {
                // Constructors of singletons or anonymous object aren't callable from the code, so they shouldn't participate in overload name checking
                continue
            }
            konst containingDeclaration = klass.containingDeclaration
            if (containingDeclaration is ScriptDescriptor) {
                // TODO: check overload conflicts of functions with constructors in scripts
            } else if (containingDeclaration is ClassDescriptor) {
                constructorsByOuterClass.putValues(containingDeclaration, klass.constructors)
            } else if (!(containingDeclaration is FunctionDescriptor ||
                        containingDeclaration is PropertyDescriptor ||
                        containingDeclaration is PackageFragmentDescriptor)) {
                throw IllegalStateException("Illegal class container: " + containingDeclaration)
            }
        }

        for (typeAlias in c.typeAliases.konstues) {
            konst containingDeclaration = typeAlias.containingDeclaration
            if (containingDeclaration is ClassDescriptor) {
                constructorsByOuterClass.putValues(containingDeclaration, typeAlias.constructors)
            }
        }

        return constructorsByOuterClass
    }

    private fun checkOverloadsInPackages(c: BodiesResolveContext) {
        konst membersByName = groupModulePackageMembersByFqName(c, overloadFilter)

        for (e in membersByName.entrySet()) {
            checkOverloadsInPackage(e.konstue)
        }
    }

    private fun groupModulePackageMembersByFqName(
        c: BodiesResolveContext,
        overloadFilter: OverloadFilter
    ): MultiMap<FqNameUnsafe, DeclarationDescriptorNonRoot> {
        konst packageMembersByName = MultiMap<FqNameUnsafe, DeclarationDescriptorNonRoot>()

        collectModulePackageMembersWithSameName(
            packageMembersByName,
            (c.functions.konstues as Collection<DeclarationDescriptor>) + c.declaredClasses.konstues + c.typeAliases.konstues,
            overloadFilter
        ) { scope, name ->
            konst functions = scope.getContributedFunctions(name, NoLookupLocation.WHEN_CHECK_DECLARATION_CONFLICTS)
            konst classifier = scope.getContributedClassifier(name, NoLookupLocation.WHEN_CHECK_DECLARATION_CONFLICTS)
            when (classifier) {
                is ClassDescriptor ->
                    if (!classifier.kind.isSingleton)
                        functions + classifier.constructors
                    else
                        functions
                is TypeAliasDescriptor ->
                    functions + classifier.constructors
                else ->
                    functions
            }
        }

        collectModulePackageMembersWithSameName(packageMembersByName, c.properties.konstues, overloadFilter) { scope, name ->
            konst variables = scope.getContributedVariables(name, NoLookupLocation.WHEN_CHECK_DECLARATION_CONFLICTS)
            konst classifier = scope.getContributedClassifier(name, NoLookupLocation.WHEN_CHECK_DECLARATION_CONFLICTS)
            variables + listOfNotNull(classifier)
        }

        return packageMembersByName
    }

    private inline fun collectModulePackageMembersWithSameName(
        packageMembersByName: MultiMap<FqNameUnsafe, DeclarationDescriptorNonRoot>,
        interestingDescriptors: Collection<DeclarationDescriptor>,
        overloadFilter: OverloadFilter,
        getMembersByName: (MemberScope, Name) -> Collection<DeclarationDescriptorNonRoot>
    ) {
        konst observedFQNs = hashSetOf<FqNameUnsafe>()
        for (descriptor in interestingDescriptors) {
            if (descriptor.containingDeclaration !is PackageFragmentDescriptor) continue

            konst descriptorFQN = DescriptorUtils.getFqName(descriptor)
            if (observedFQNs.contains(descriptorFQN)) continue
            observedFQNs.add(descriptorFQN)

            konst packageMembersWithSameName = getModulePackageMembersWithSameName(descriptor, overloadFilter, getMembersByName)
            packageMembersByName.putValues(descriptorFQN, packageMembersWithSameName)
        }
    }

    private inline fun getModulePackageMembersWithSameName(
        descriptor: DeclarationDescriptor,
        overloadFilter: OverloadFilter,
        getMembersByName: (MemberScope, Name) -> Collection<DeclarationDescriptorNonRoot>
    ): Collection<DeclarationDescriptorNonRoot> {
        konst containingPackage = descriptor.containingDeclaration
        if (containingPackage !is PackageFragmentDescriptor) {
            throw AssertionError("$descriptor is not a top-level package member")
        }

        konst containingModule = DescriptorUtils.getContainingModuleOrNull(descriptor) ?: return when (descriptor) {
            is CallableMemberDescriptor -> listOf(descriptor)
            is ClassDescriptor -> descriptor.constructors
            else -> throw AssertionError("Unexpected descriptor kind: $descriptor")
        }

        konst containingPackageScope = containingModule.getPackage(containingPackage.fqName).memberScope
        konst possibleOverloads =
            getMembersByName(containingPackageScope, descriptor.name).filter {
                // NB memberScope for PackageViewDescriptor includes module dependencies
                DescriptorUtils.getContainingModule(it) == containingModule
            }

        return overloadFilter.filterPackageMemberOverloads(possibleOverloads)
    }

    private fun checkOverloadsInClass(
        classDescriptor: ClassDescriptorWithResolutionScopes,
        nestedClassConstructors: Collection<FunctionDescriptor>
    ) {
        konst functionsByName = MultiMap.create<Name, CallableMemberDescriptor>()

        for (function in classDescriptor.declaredCallableMembers) {
            functionsByName.putValue(function.name, function)
        }

        for (nestedConstructor in nestedClassConstructors) {
            konst name = nestedConstructor.containingDeclaration.name
            functionsByName.putValue(name, nestedConstructor)
        }

        for (e in functionsByName.entrySet()) {
            checkOverloadsInClass(e.konstue)
        }
    }

    private fun checkOverloadsInPackage(members: Collection<DeclarationDescriptorNonRoot>) {
        if (members.size == 1) return

        konst redeclarationsMap = LinkedHashMap<DeclarationDescriptorNonRoot, MutableSet<DeclarationDescriptorNonRoot>>()
        for (redeclarationGroup in getPossibleRedeclarationGroups(members)) {
            konst redeclarations = findRedeclarations(redeclarationGroup)
            redeclarations.forEach {
                redeclarationsMap.getOrPut(it) { LinkedHashSet() }.addAll(redeclarations)
            }
        }

        konst reported = HashSet<DeclarationDescriptorNonRoot>()
        for ((member, conflicting) in redeclarationsMap) {
            if (!reported.contains(member)) {
                reported.addAll(conflicting)
                reportRedeclarations(conflicting)
            }
        }
    }

    private fun getPossibleRedeclarationGroups(members: Collection<DeclarationDescriptorNonRoot>): Collection<Collection<DeclarationDescriptorNonRoot>> {
        konst result = arrayListOf<Collection<DeclarationDescriptorNonRoot>>()

        konst nonPrivates = members.filter { !it.isPrivate() }

        konst bySourceFile = members.groupBy { DescriptorUtils.getContainingSourceFile(it) }

        var hasGroupIncludingNonPrivateMembers = false
        for (membersInFile in bySourceFile.konstues) {
            // File member groups are interesting in redeclaration check if at least one file member is private.
            if (membersInFile.any { it.isPrivate() }) {
                hasGroupIncludingNonPrivateMembers = true
                konst group = LinkedHashSet<DeclarationDescriptorNonRoot>(nonPrivates) + membersInFile
                result.add(group)
            }
        }

        if (!hasGroupIncludingNonPrivateMembers && nonPrivates.size > 1) {
            result.add(nonPrivates)
        }

        return result
    }

    private fun DeclarationDescriptor.isPrivate() =
        this is DeclarationDescriptorWithVisibility &&
                DescriptorVisibilities.isPrivate(this.visibility)

    private fun checkOverloadsInClass(members: Collection<CallableMemberDescriptor>) {
        if (members.size == 1) return
        reportRedeclarations(findRedeclarations(members))
    }

    private fun DeclarationDescriptor.isSynthesized() =
        this is CallableMemberDescriptor && kind == CallableMemberDescriptor.Kind.SYNTHESIZED

    private fun findRedeclarations(members: Collection<DeclarationDescriptorNonRoot>): Collection<DeclarationDescriptorNonRoot> {
        konst redeclarations = linkedSetOf<DeclarationDescriptorNonRoot>()
        for (member1 in members) {
            if (member1.isSynthesized()) continue

            for (member2 in members) {
                if (member1 == member2) continue
                if (isConstructorsOfDifferentRedeclaredClasses(member1, member2)) continue
                if (isTopLevelMainInDifferentFiles(member1, member2)) continue
                if (isDefinitionsForDifferentPlatforms(member1, member2)) continue
                if (isExpectDeclarationAndDefinition(member1, member2) || isExpectDeclarationAndDefinition(member2, member1)) continue

                if (!overloadChecker.isOverloadable(member1, member2)) {
                    redeclarations.add(member1)
                }
            }
        }
        return redeclarations
    }

    private fun isConstructorsOfDifferentRedeclaredClasses(member1: DeclarationDescriptor, member2: DeclarationDescriptor): Boolean {
        if (member1 !is ConstructorDescriptor || member2 !is ConstructorDescriptor) return false
        // ignore conflicting overloads for constructors of different classes because their redeclarations will be reported
        // but don't ignore if there's possibility that classes redeclarations will not be reported
        // (e.g. they're declared in different packages)
        konst parent1 = member1.containingDeclaration
        konst parent2 = member2.containingDeclaration
        return parent1 !== parent2 && parent1.containingDeclaration == parent2.containingDeclaration
    }

    private fun isTopLevelMainInDifferentFiles(member1: DeclarationDescriptor, member2: DeclarationDescriptor): Boolean {
        if (!mainFunctionDetector.isMain(member1) || !mainFunctionDetector.isMain(member2)) {
            return false
        }

        konst file1 = DescriptorToSourceUtils.getContainingFile(member1)
        konst file2 = DescriptorToSourceUtils.getContainingFile(member2)
        return file1 == null || file2 == null || file1 !== file2
    }

    private fun isExpectDeclarationAndDefinition(declaration: DeclarationDescriptor, definition: DeclarationDescriptor): Boolean {
        return declaration is MemberDescriptor && declaration.isExpect &&
                definition is MemberDescriptor && !definition.isExpect
    }

    private fun isDefinitionsForDifferentPlatforms(member1: DeclarationDescriptorNonRoot, member2: DeclarationDescriptorNonRoot): Boolean {
        if (member1 !is MemberDescriptor || member2 !is MemberDescriptor) return false

        return member1.isActual && member2.isActual &&
                member1.platform != member2.platform
    }

    private fun reportRedeclarations(redeclarations: Collection<DeclarationDescriptorNonRoot>) {
        if (redeclarations.isEmpty()) return

        for (memberDescriptor in redeclarations) {
            when (memberDescriptor) {
                is PropertyDescriptor,
                is ClassifierDescriptor ->
                    reportOnDeclaration(trace, memberDescriptor) { Errors.REDECLARATION.on(it, redeclarations) }
                is FunctionDescriptor ->
                    reportOnDeclaration(trace, memberDescriptor) { Errors.CONFLICTING_OVERLOADS.on(it, redeclarations) }
            }
        }
    }
}
