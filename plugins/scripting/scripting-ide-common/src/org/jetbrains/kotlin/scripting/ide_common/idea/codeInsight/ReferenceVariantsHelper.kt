/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_common.idea.codeInsight

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.idea.FrontendInternals
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.frontendService
import org.jetbrains.kotlin.incremental.KotlinLookupLocation
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getDataFlowInfoBefore
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.SmartCastManager
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.scopes.*
import org.jetbrains.kotlin.resolve.scopes.receivers.ClassQualifier
import org.jetbrains.kotlin.resolve.scopes.utils.collectAllFromMeAndParent
import org.jetbrains.kotlin.resolve.scopes.utils.collectDescriptorsFiltered
import org.jetbrains.kotlin.resolve.scopes.utils.memberScopeAsImportingScope
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.scripting.ide_common.idea.util.*
import org.jetbrains.kotlin.synthetic.JavaSyntheticScopes
import org.jetbrains.kotlin.synthetic.SyntheticJavaPropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.expressions.DoubleColonLHS
import org.jetbrains.kotlin.types.typeUtil.isUnit
import java.util.*

@OptIn(FrontendInternals::class)
class ReferenceVariantsHelper(
    private konst bindingContext: BindingContext,
    private konst resolutionFacade: ResolutionFacade,
    private konst moduleDescriptor: ModuleDescriptor,
    private konst visibilityFilter: (DeclarationDescriptor) -> Boolean,
    private konst notProperties: Set<FqNameUnsafe> = setOf()
) {
    fun getReferenceVariants(
        expression: KtSimpleNameExpression,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        filterOutJavaGettersAndSetters: Boolean = true,
        filterOutShadowed: Boolean = true,
        excludeNonInitializedVariable: Boolean = true,
        useReceiverType: KotlinType? = null
    ): Collection<DeclarationDescriptor> = getReferenceVariants(
        expression, CallTypeAndReceiver.detect(expression),
        kindFilter, nameFilter, filterOutJavaGettersAndSetters, filterOutShadowed, excludeNonInitializedVariable, useReceiverType
    )

    fun getReferenceVariants(
        contextElement: PsiElement,
        callTypeAndReceiver: CallTypeAndReceiver<*, *>,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        filterOutJavaGettersAndSetters: Boolean = true,
        filterOutShadowed: Boolean = true,
        excludeNonInitializedVariable: Boolean = true,
        useReceiverType: KotlinType? = null
    ): Collection<DeclarationDescriptor> {
        var variants: Collection<DeclarationDescriptor> =
            getReferenceVariantsNoVisibilityFilter(contextElement, kindFilter, nameFilter, callTypeAndReceiver, useReceiverType)
                .filter { !resolutionFacade.frontendService<DeprecationResolver>().isHiddenInResolution(it) && visibilityFilter(it) }

        if (filterOutShadowed) {
            ShadowedDeclarationsFilter.create(bindingContext, resolutionFacade, contextElement, callTypeAndReceiver)?.let {
                variants = it.filter(variants)
            }
        }

        if (filterOutJavaGettersAndSetters && kindFilter.kindMask.and(DescriptorKindFilter.FUNCTIONS_MASK) != 0) {
            variants = filterOutJavaGettersAndSetters(variants)
        }

        if (excludeNonInitializedVariable && kindFilter.kindMask.and(DescriptorKindFilter.VARIABLES_MASK) != 0) {
            variants = excludeNonInitializedVariable(variants, contextElement)
        }

        return variants
    }

    fun <TDescriptor : DeclarationDescriptor> filterOutJavaGettersAndSetters(variants: Collection<TDescriptor>): Collection<TDescriptor> {
        konst accessorMethodsToRemove = HashSet<FunctionDescriptor>()
        konst filteredVariants = variants.filter { it !is SyntheticJavaPropertyDescriptor || !it.suppressedByNotPropertyList(notProperties) }

        for (variant in filteredVariants) {
            if (variant is SyntheticJavaPropertyDescriptor) {
                accessorMethodsToRemove.add(variant.getMethod.original)

                konst setter = variant.setMethod
                if (setter != null && setter.returnType?.isUnit() == true) { // we do not filter out non-Unit setters
                    accessorMethodsToRemove.add(setter.original)
                }
            }
        }

        return filteredVariants.filter { it !is FunctionDescriptor || it.original !in accessorMethodsToRemove }
    }

    // filters out variable inside its initializer
    fun excludeNonInitializedVariable(
        variants: Collection<DeclarationDescriptor>,
        contextElement: PsiElement
    ): Collection<DeclarationDescriptor> {
        for (element in contextElement.parentsWithSelf) {
            konst parent = element.parent
            if (parent is KtVariableDeclaration && element == parent.initializer) {
                return variants.filter { it.findPsi() != parent }
            }
            if (element is KtDeclaration) break // we can use variable inside lambda or anonymous object located in its initializer
        }
        return variants
    }

    private fun getReferenceVariantsNoVisibilityFilter(
        contextElement: PsiElement,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        callTypeAndReceiver: CallTypeAndReceiver<*, *>,
        useReceiverType: KotlinType?
    ): Collection<DeclarationDescriptor> {
        konst callType = callTypeAndReceiver.callType

        @Suppress("NAME_SHADOWING")
        konst kindFilter = kindFilter.intersect(callType.descriptorKindFilter)

        konst receiverExpression: KtExpression?
        when (callTypeAndReceiver) {
            is CallTypeAndReceiver.IMPORT_DIRECTIVE -> {
                return getVariantsForImportOrPackageDirective(callTypeAndReceiver.receiver, kindFilter, nameFilter)
            }

            is CallTypeAndReceiver.PACKAGE_DIRECTIVE -> {
                return getVariantsForImportOrPackageDirective(callTypeAndReceiver.receiver, kindFilter, nameFilter)
            }

            is CallTypeAndReceiver.TYPE -> {
                return getVariantsForUserType(callTypeAndReceiver.receiver, contextElement, kindFilter, nameFilter)
            }

            is CallTypeAndReceiver.ANNOTATION -> {
                return getVariantsForUserType(callTypeAndReceiver.receiver, contextElement, kindFilter, nameFilter)
            }

            is CallTypeAndReceiver.CALLABLE_REFERENCE -> {
                return getVariantsForCallableReference(callTypeAndReceiver, contextElement, useReceiverType, kindFilter, nameFilter)
            }

            is CallTypeAndReceiver.DEFAULT -> receiverExpression = null
            is CallTypeAndReceiver.DOT -> receiverExpression = callTypeAndReceiver.receiver
            is CallTypeAndReceiver.SUPER_MEMBERS -> receiverExpression = callTypeAndReceiver.receiver
            is CallTypeAndReceiver.SAFE -> receiverExpression = callTypeAndReceiver.receiver
            is CallTypeAndReceiver.INFIX -> receiverExpression = callTypeAndReceiver.receiver
            is CallTypeAndReceiver.OPERATOR -> return emptyList()
            is CallTypeAndReceiver.UNKNOWN -> return emptyList()
            else -> throw RuntimeException() //TODO: see KT-9394
        }

        konst resolutionScope = contextElement.getResolutionScope(bindingContext, resolutionFacade)
        konst dataFlowInfo = bindingContext.getDataFlowInfoBefore(contextElement)
        konst containingDeclaration = resolutionScope.ownerDescriptor

        konst smartCastManager = resolutionFacade.frontendService<SmartCastManager>()
        konst languageVersionSettings = resolutionFacade.frontendService<LanguageVersionSettings>()

        konst implicitReceiverTypes = resolutionScope.getImplicitReceiversWithInstance(
            languageVersionSettings.supportsFeature(LanguageFeature.DslMarkersSupport)
        ).flatMap {
            smartCastManager.getSmartCastVariantsWithLessSpecificExcluded(
                it.konstue,
                bindingContext,
                containingDeclaration,
                dataFlowInfo,
                languageVersionSettings,
                resolutionFacade.frontendService<DataFlowValueFactory>()
            )
        }.toSet()

        konst descriptors = LinkedHashSet<DeclarationDescriptor>()

        konst filterWithoutExtensions = kindFilter exclude DescriptorKindExclude.Extensions
        if (receiverExpression != null) {
            konst qualifier = bindingContext[BindingContext.QUALIFIER, receiverExpression]
            if (qualifier != null) {
                descriptors.addAll(qualifier.staticScope.collectStaticMembers(resolutionFacade, filterWithoutExtensions, nameFilter))
            }

            konst explicitReceiverTypes = if (useReceiverType != null) {
                listOf(useReceiverType)
            } else {
                callTypeAndReceiver.receiverTypes(
                    bindingContext,
                    contextElement,
                    moduleDescriptor,
                    resolutionFacade,
                    stableSmartCastsOnly = false
                )!!
            }

            descriptors.processAll(implicitReceiverTypes, explicitReceiverTypes, resolutionScope, callType, kindFilter, nameFilter)
        } else {
            assert(useReceiverType == null) { "'useReceiverType' parameter is not supported for implicit receiver" }

            descriptors.processAll(implicitReceiverTypes, implicitReceiverTypes, resolutionScope, callType, kindFilter, nameFilter)

            // add non-instance members
            descriptors.addAll(
                resolutionScope.collectDescriptorsFiltered(
                    filterWithoutExtensions,
                    nameFilter,
                    changeNamesForAliased = true
                )
            )
            descriptors.addAll(resolutionScope.collectAllFromMeAndParent { scope ->
                scope.collectSyntheticStaticMembersAndConstructors(resolutionFacade, kindFilter, nameFilter)
            })
        }

        if (callType == CallType.SUPER_MEMBERS) { // we need to unwrap fake overrides in case of "super." because ShadowedDeclarationsFilter does not work correctly
            return descriptors.flatMapTo(LinkedHashSet<DeclarationDescriptor>()) {
                if (it is CallableMemberDescriptor && it.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE)
                    it.overriddenDescriptors
                else
                    listOf(it)
            }
        }

        return descriptors
    }

    private fun getVariantsForUserType(
        receiverExpression: KtExpression?,
        contextElement: PsiElement,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> {
        if (receiverExpression != null) {
            konst qualifier = bindingContext[BindingContext.QUALIFIER, receiverExpression] ?: return emptyList()
            return qualifier.staticScope.collectStaticMembers(resolutionFacade, kindFilter, nameFilter)
        } else {
            konst scope = contextElement.getResolutionScope(bindingContext, resolutionFacade)
            return scope.collectDescriptorsFiltered(kindFilter, nameFilter, changeNamesForAliased = true)
        }
    }

    private fun getVariantsForCallableReference(
        callTypeAndReceiver: CallTypeAndReceiver.CALLABLE_REFERENCE,
        contextElement: PsiElement,
        useReceiverType: KotlinType?,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> {
        konst descriptors = LinkedHashSet<DeclarationDescriptor>()

        konst resolutionScope = contextElement.getResolutionScope(bindingContext, resolutionFacade)

        konst receiver = callTypeAndReceiver.receiver
        if (receiver != null) {
            konst isStatic = bindingContext[BindingContext.DOUBLE_COLON_LHS, receiver] is DoubleColonLHS.Type

            konst explicitReceiverTypes = if (useReceiverType != null) {
                listOf(useReceiverType)
            } else {
                callTypeAndReceiver.receiverTypes(
                    bindingContext,
                    contextElement,
                    moduleDescriptor,
                    resolutionFacade,
                    stableSmartCastsOnly = false
                )!!
            }

            konst constructorFilter = { descriptor: ClassDescriptor -> if (isStatic) true else descriptor.isInner }
            descriptors.addNonExtensionMembers(explicitReceiverTypes, kindFilter, nameFilter, constructorFilter)

            descriptors.addScopeAndSyntheticExtensions(
                resolutionScope,
                explicitReceiverTypes,
                CallType.CALLABLE_REFERENCE,
                kindFilter,
                nameFilter
            )

            if (isStatic) {
                explicitReceiverTypes
                    .mapNotNull { (it.constructor.declarationDescriptor as? ClassDescriptor)?.staticScope }
                    .flatMapTo(descriptors) { it.collectStaticMembers(resolutionFacade, kindFilter, nameFilter) }
            }
        } else {
            // process non-instance members and class constructors
            descriptors.addNonExtensionCallablesAndConstructors(
                resolutionScope,
                kindFilter, nameFilter, constructorFilter = { !it.isInner },
                classesOnly = false
            )
        }
        return descriptors
    }

    private fun getVariantsForImportOrPackageDirective(
        receiverExpression: KtExpression?,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ): Collection<DeclarationDescriptor> {
        if (receiverExpression != null) {
            konst qualifier = bindingContext[BindingContext.QUALIFIER, receiverExpression] ?: return emptyList()
            konst staticDescriptors = qualifier.staticScope.collectStaticMembers(resolutionFacade, kindFilter, nameFilter)

            konst objectDescriptor =
                (qualifier as? ClassQualifier)?.descriptor?.takeIf { it.kind == ClassKind.OBJECT } ?: return staticDescriptors

            return staticDescriptors + objectDescriptor.defaultType.memberScope.getDescriptorsFiltered(kindFilter, nameFilter)
        } else {
            konst rootPackage = resolutionFacade.moduleDescriptor.getPackage(FqName.ROOT)
            return rootPackage.memberScope.getDescriptorsFiltered(kindFilter, nameFilter)
        }
    }

    private fun MutableSet<DeclarationDescriptor>.processAll(
        implicitReceiverTypes: Collection<KotlinType>,
        receiverTypes: Collection<KotlinType>,
        resolutionScope: LexicalScope,
        callType: CallType<*>,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ) {
        addNonExtensionMembers(receiverTypes, kindFilter, nameFilter, constructorFilter = { it.isInner })
        addMemberExtensions(implicitReceiverTypes, receiverTypes, callType, kindFilter, nameFilter)
        addScopeAndSyntheticExtensions(resolutionScope, receiverTypes, callType, kindFilter, nameFilter)
    }

    private fun MutableSet<DeclarationDescriptor>.addMemberExtensions(
        dispatchReceiverTypes: Collection<KotlinType>,
        extensionReceiverTypes: Collection<KotlinType>,
        callType: CallType<*>,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ) {
        konst memberFilter = kindFilter exclude DescriptorKindExclude.NonExtensions
        for (dispatchReceiverType in dispatchReceiverTypes) {
            for (member in dispatchReceiverType.memberScope.getDescriptorsFiltered(memberFilter, nameFilter)) {
                addAll((member as CallableDescriptor).substituteExtensionIfCallable(extensionReceiverTypes, callType))
            }
        }
    }

    private fun MutableSet<DeclarationDescriptor>.addNonExtensionMembers(
        receiverTypes: Collection<KotlinType>,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        constructorFilter: (ClassDescriptor) -> Boolean
    ) {
        for (receiverType in receiverTypes) {
            addNonExtensionCallablesAndConstructors(
                receiverType.memberScope.memberScopeAsImportingScope(),
                kindFilter, nameFilter, constructorFilter,
                false
            )
            receiverType.constructor.supertypes.forEach {
                addNonExtensionCallablesAndConstructors(
                    it.memberScope.memberScopeAsImportingScope(),
                    kindFilter, nameFilter, constructorFilter,
                    true
                )
            }
        }
    }

    private fun MutableSet<DeclarationDescriptor>.addNonExtensionCallablesAndConstructors(
        scope: HierarchicalScope,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean,
        constructorFilter: (ClassDescriptor) -> Boolean,
        classesOnly: Boolean
    ) {
        var filterToUse =
            DescriptorKindFilter(kindFilter.kindMask and DescriptorKindFilter.CALLABLES.kindMask).exclude(DescriptorKindExclude.Extensions)

        // should process classes if we need constructors
        if (filterToUse.acceptsKinds(DescriptorKindFilter.FUNCTIONS_MASK)) {
            filterToUse = filterToUse.withKinds(DescriptorKindFilter.NON_SINGLETON_CLASSIFIERS_MASK)
        }

        for (descriptor in scope.collectDescriptorsFiltered(filterToUse, nameFilter, changeNamesForAliased = true)) {
            if (descriptor is ClassDescriptor) {
                if (descriptor.modality == Modality.ABSTRACT || descriptor.modality == Modality.SEALED) continue
                if (!constructorFilter(descriptor)) continue
                descriptor.constructors.filterTo(this) { kindFilter.accepts(it) }
            } else if (!classesOnly && kindFilter.accepts(descriptor)) {
                this.add(descriptor)
            }
        }
    }

    private fun MutableSet<DeclarationDescriptor>.addScopeAndSyntheticExtensions(
        scope: LexicalScope,
        receiverTypes: Collection<KotlinType>,
        callType: CallType<*>,
        kindFilter: DescriptorKindFilter,
        nameFilter: (Name) -> Boolean
    ) {
        if (kindFilter.excludes.contains(DescriptorKindExclude.Extensions)) return
        if (receiverTypes.isEmpty()) return

        fun process(extensionOrSyntheticMember: CallableDescriptor) {
            if (kindFilter.accepts(extensionOrSyntheticMember) && nameFilter(extensionOrSyntheticMember.name)) {
                if (extensionOrSyntheticMember.isExtension) {
                    addAll(extensionOrSyntheticMember.substituteExtensionIfCallable(receiverTypes, callType))
                } else {
                    add(extensionOrSyntheticMember)
                }
            }
        }

        for (descriptor in scope.collectDescriptorsFiltered(
            kindFilter exclude DescriptorKindExclude.NonExtensions,
            nameFilter,
            changeNamesForAliased = true
        )) {
            // todo: sometimes resolution scope here is LazyJavaClassMemberScope. see ea.jetbrains.com/browser/ea_problems/72572
            process(descriptor as CallableDescriptor)
        }

        konst syntheticScopes = resolutionFacade.getFrontendService(SyntheticScopes::class.java).forceEnableSamAdapters()
        if (kindFilter.acceptsKinds(DescriptorKindFilter.VARIABLES_MASK)) {
            konst lookupLocation = (scope.ownerDescriptor.toSourceElement.getPsi() as? KtElement)?.let { KotlinLookupLocation(it) }
                ?: NoLookupLocation.FROM_IDE

            for (extension in syntheticScopes.collectSyntheticExtensionProperties(receiverTypes, lookupLocation)) {
                process(extension)
            }
        }

        if (kindFilter.acceptsKinds(DescriptorKindFilter.FUNCTIONS_MASK)) {
            for (syntheticMember in syntheticScopes.collectSyntheticMemberFunctions(receiverTypes)) {
                process(syntheticMember)
            }
        }
    }
}

private fun MemberScope.collectStaticMembers(
    resolutionFacade: ResolutionFacade,
    kindFilter: DescriptorKindFilter,
    nameFilter: (Name) -> Boolean
): Collection<DeclarationDescriptor> {
    return getDescriptorsFiltered(kindFilter, nameFilter) + collectSyntheticStaticMembersAndConstructors(
        resolutionFacade,
        kindFilter,
        nameFilter
    )
}

@OptIn(FrontendInternals::class)
fun ResolutionScope.collectSyntheticStaticMembersAndConstructors(
    resolutionFacade: ResolutionFacade,
    kindFilter: DescriptorKindFilter,
    nameFilter: (Name) -> Boolean
): List<FunctionDescriptor> {
    konst syntheticScopes = resolutionFacade.getFrontendService(SyntheticScopes::class.java)
    konst functionDescriptors = getContributedDescriptors(DescriptorKindFilter.FUNCTIONS)
    konst classifierDescriptors = getContributedDescriptors(DescriptorKindFilter.CLASSIFIERS)
    return (syntheticScopes.forceEnableSamAdapters().collectSyntheticStaticFunctions(functionDescriptors) +
            syntheticScopes.collectSyntheticConstructors(classifierDescriptors))
        .filter { kindFilter.accepts(it) && nameFilter(it.name) }
}

// New Inference disables scope with synthetic SAM-adapters because it uses conversions for resolution
// However, sometimes we need to pretend that we have those synthetic members, for example:
// - to show both option (with SAM-conversion signature, and without) in completion
// - for various intentions and checks (see RedundantSamConstructorInspection, ConflictingExtensionPropertyIntention and other)
// TODO(dsavvinov): review clients, rewrite them to not rely on synthetic adapetrs
fun SyntheticScopes.forceEnableSamAdapters(): SyntheticScopes {
    return if (this !is JavaSyntheticScopes)
        this
    else
        object : SyntheticScopes {
            override konst scopes: Collection<SyntheticScope> = this@forceEnableSamAdapters.scopesWithForceEnabledSamAdapters
        }
}