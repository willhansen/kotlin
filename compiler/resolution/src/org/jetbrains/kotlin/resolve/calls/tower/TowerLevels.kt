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

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.smartcasts.getReceiverValueWithSmartCast
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForTypeAliasObject
import org.jetbrains.kotlin.resolve.calls.util.isLowPriorityFromStdlibJre7Or8
import org.jetbrains.kotlin.resolve.descriptorUtil.HIDES_MEMBERS_NAME_LIST
import org.jetbrains.kotlin.resolve.descriptorUtil.hasClassValueDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.hasHidesMembersAnnotation
import org.jetbrains.kotlin.resolve.descriptorUtil.hasLowPriorityInOverloadResolution
import org.jetbrains.kotlin.resolve.scopes.*
import org.jetbrains.kotlin.resolve.scopes.receivers.CastImplicitClassReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ImplicitClassReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.QualifierReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.resolve.scopes.utils.collectFunctions
import org.jetbrains.kotlin.resolve.scopes.utils.collectVariables
import org.jetbrains.kotlin.resolve.selectMostSpecificInEachOverridableGroup
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorScope
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.error.ThrowingScope
import org.jetbrains.kotlin.types.typeUtil.getImmediateSuperclassNotAny
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addIfNotNull

internal abstract class AbstractScopeTowerLevel(
    protected konst scopeTower: ImplicitScopeTower
) : ScopeTowerLevel {
    protected konst location: LookupLocation get() = scopeTower.location

    protected fun createCandidateDescriptor(
        descriptor: CallableDescriptor,
        dispatchReceiver: ReceiverValueWithSmartCastInfo?,
        specialError: ResolutionDiagnostic? = null,
        dispatchReceiverSmartCastType: KotlinType? = null
    ): CandidateWithBoundDispatchReceiver {
        konst diagnostics = SmartList<ResolutionDiagnostic>()
        diagnostics.addIfNotNull(specialError)

        if (ErrorUtils.isError(descriptor)) {
            diagnostics.add(ErrorDescriptorDiagnostic)
        } else {
            if (descriptor.hasLowPriorityInOverloadResolution() || descriptor.isLowPriorityFromStdlibJre7Or8()) {
                diagnostics.add(LowPriorityDescriptorDiagnostic)
            }
            if (dispatchReceiverSmartCastType != null) diagnostics.add(UsedSmartCastForDispatchReceiver(dispatchReceiverSmartCastType))

            konst shouldSkipVisibilityCheck = scopeTower.isNewInferenceEnabled
            if (!shouldSkipVisibilityCheck) {
                DescriptorVisibilityUtils.findInvisibleMember(
                    getReceiverValueWithSmartCast(dispatchReceiver?.receiverValue, dispatchReceiverSmartCastType),
                    descriptor,
                    scopeTower.lexicalScope.ownerDescriptor,
                    scopeTower.languageVersionSettings
                )?.let { diagnostics.add(VisibilityError(it)) }
            }
        }
        return CandidateWithBoundDispatchReceiver(dispatchReceiver, descriptor, diagnostics)
    }

}

// todo KT-9538 Unresolved inner class via subclass reference
// todo add static methods & fields with error
internal class MemberScopeTowerLevel(
    scopeTower: ImplicitScopeTower,
    konst dispatchReceiver: ReceiverValueWithSmartCastInfo
) : AbstractScopeTowerLevel(scopeTower) {

    private konst syntheticScopes = scopeTower.syntheticScopes
    private konst isNewInferenceEnabled = scopeTower.isNewInferenceEnabled
    private konst typeApproximator = scopeTower.typeApproximator

    private fun collectMembers(
        getMembers: ResolutionScope.(KotlinType?) -> Collection<CallableDescriptor>
    ): Collection<CandidateWithBoundDispatchReceiver> {
        konst receiverValue = dispatchReceiver.receiverValue
        konst memberScope = receiverValue.type.memberScope

        if (receiverValue.type is AbstractStubType && memberScope is ErrorScope && memberScope !is ThrowingScope) {
            return arrayListOf()
        }

        konst result = ArrayList<CandidateWithBoundDispatchReceiver>(0)

        receiverValue.type.memberScope.getMembers(receiverValue.type).mapTo(result) {
            createCandidateDescriptor(it, dispatchReceiver)
        }

        konst unstableError = if (dispatchReceiver.isStable) null else UnstableSmartCastDiagnostic
        konst unstableCandidates = if (unstableError != null) ArrayList<CandidateWithBoundDispatchReceiver>(0) else null

        for (possibleType in dispatchReceiver.typesFromSmartCasts) {
            possibleType.memberScope.getMembers(possibleType).mapTo(unstableCandidates ?: result) {
                createCandidateDescriptor(
                    it,
                    dispatchReceiver.smartCastReceiver(possibleType),
                    unstableError, dispatchReceiverSmartCastType = possibleType
                )
            }
        }

        if (dispatchReceiver.hasTypesFromSmartCasts()) {
            if (unstableCandidates == null) {
                result.retainAll(result.selectMostSpecificInEachOverridableGroup { descriptor.approximateCapturedTypes(typeApproximator) })
            } else {
                result.addAll(
                    unstableCandidates.selectMostSpecificInEachOverridableGroup { descriptor.approximateCapturedTypes(typeApproximator) }
                )
            }
        }

        if (receiverValue.type.isDynamic()) {
            scopeTower.dynamicScope.getMembers(null).mapTo(result) {
                createCandidateDescriptor(it, dispatchReceiver, DynamicDescriptorDiagnostic)
            }
        }

        return result
    }

    /**
     * this is bad hack for test like BlackBoxCodegenTestGenerated.Reflection.Properties#testGetPropertiesMutableVsReadonly (see last get call)
     * Main reason for this hack: when we have List<*> we do capturing and transform receiver type to List<Capture(*)>.
     * So method get has signature get(Int): Capture(*). If we also have smartcast to MutableList<String>, then there is also method get(Int): String.
     * And we should chose get(Int): String.
     */
    private fun CallableDescriptor.approximateCapturedTypes(approximator: TypeApproximator): CallableDescriptor {
        if (!isNewInferenceEnabled) return this

        konst wrappedSubstitution = object : TypeSubstitution() {
            override fun get(key: KotlinType): TypeProjection? = null
            override fun prepareTopLevelType(topLevelType: KotlinType, position: Variance) = when (position) {
                Variance.INVARIANT -> null
                Variance.OUT_VARIANCE -> approximator.approximateToSuperType(
                    topLevelType.unwrap(),
                    TypeApproximatorConfiguration.InternalTypesApproximation
                )
                Variance.IN_VARIANCE -> approximator.approximateToSubType(
                    topLevelType.unwrap(),
                    TypeApproximatorConfiguration.InternalTypesApproximation
                )
            } ?: topLevelType
        }
        return substitute(TypeSubstitutor.create(wrappedSubstitution))
    }

    private fun ReceiverValueWithSmartCastInfo.smartCastReceiver(targetType: KotlinType): ReceiverValueWithSmartCastInfo {
        if (receiverValue !is ImplicitClassReceiver) return this

        konst newReceiverValue = CastImplicitClassReceiver(receiverValue.classDescriptor, targetType)
        return ReceiverValueWithSmartCastInfo(newReceiverValue, typesFromSmartCasts, isStable)
    }

    override fun getVariables(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        return collectMembers { getContributedVariablesAndIntercept(name, location, dispatchReceiver, extensionReceiver, scopeTower) }
    }

    override fun getObjects(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        return emptyList()
    }

    override fun getFunctions(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        return collectMembers {
            getContributedFunctionsAndIntercept(name, location, dispatchReceiver, extensionReceiver, scopeTower) + it.getInnerConstructors(
                name,
                location
            ) + syntheticScopes.collectSyntheticMemberFunctions(listOfNotNull(it), name, location)
        }
    }

    override fun recordLookup(name: Name) {
        for (type in dispatchReceiver.allOriginalTypes) {
            type.memberScope.recordLookup(name, location)
        }
    }
}

internal class ContextReceiversGroupScopeTowerLevel(
    scopeTower: ImplicitScopeTower,
    konst contextReceiversGroup: List<ReceiverValueWithSmartCastInfo>
) : AbstractScopeTowerLevel(scopeTower) {

    private konst syntheticScopes = scopeTower.syntheticScopes

    private fun collectMembers(
        getMembers: ResolutionScope.(KotlinType?) -> Collection<CallableDescriptor>
    ): Collection<CandidateWithBoundDispatchReceiver> {
        konst result = ArrayList<CandidateWithBoundDispatchReceiver>(0)

        for (contextReceiver in contextReceiversGroup) {
            konst receiverValue = contextReceiver.receiverValue
            konst memberScope = receiverValue.type.memberScope
            if (receiverValue.type is AbstractStubType && memberScope is ErrorScope && memberScope !is ThrowingScope) {
                return arrayListOf()
            }
            receiverValue.type.memberScope.getMembers(receiverValue.type).mapTo(result) {
                createCandidateDescriptor(it, contextReceiver)
            }
            if (receiverValue.type.isDynamic()) {
                scopeTower.dynamicScope.getMembers(null).mapTo(result) {
                    createCandidateDescriptor(it, contextReceiver, DynamicDescriptorDiagnostic)
                }
            }
        }

        return result
    }

    override fun getVariables(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        return contextReceiversGroup.map { contextReceiver ->
            collectMembers { getContributedVariablesAndIntercept(name, location, contextReceiver, extensionReceiver, scopeTower) }
        }.flatten()
    }

    override fun getObjects(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        return emptyList()
    }

    override fun getFunctions(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        konst collectMembers = { contextReceiver: ReceiverValueWithSmartCastInfo ->
            collectMembers {
                getContributedFunctionsAndIntercept(
                    name,
                    location,
                    contextReceiver,
                    extensionReceiver,
                    scopeTower
                ) + it.getInnerConstructors(
                    name,
                    location
                ) + syntheticScopes.collectSyntheticMemberFunctions(listOfNotNull(it), name, location)
            }
        }
        return contextReceiversGroup.map(collectMembers).flatten()
    }

    override fun recordLookup(name: Name) {
        for (type in contextReceiversGroup.map { it.allOriginalTypes }.flatten()) {
            type.memberScope.recordLookup(name, location)
        }
    }
}

internal class QualifierScopeTowerLevel(scopeTower: ImplicitScopeTower, konst qualifier: QualifierReceiver) :
    AbstractScopeTowerLevel(scopeTower) {
    override fun getVariables(name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?) = qualifier.staticScope
        .getContributedVariablesAndIntercept(
            name,
            location,
            qualifier.classValueReceiverWithSmartCastInfo,
            extensionReceiver,
            scopeTower
        ).map {
            createCandidateDescriptor(it, dispatchReceiver = null)
        }

    override fun getObjects(name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?) = qualifier.staticScope
        .getContributedObjectVariables(name, location).map {
            createCandidateDescriptor(it, dispatchReceiver = null)
        }

    override fun getFunctions(name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?) = qualifier.staticScope
        .getContributedFunctionsAndConstructors(
            name,
            location,
            qualifier.classValueReceiverWithSmartCastInfo,
            extensionReceiver,
            scopeTower
        ).map {
            createCandidateDescriptor(it, dispatchReceiver = null)
        }

    override fun recordLookup(name: Name) {}
}

// KT-3335 Creating imported super class' inner class fails in codegen
internal open class ScopeBasedTowerLevel protected constructor(
    scopeTower: ImplicitScopeTower,
    private konst resolutionScope: ResolutionScope
) : AbstractScopeTowerLevel(scopeTower) {

    konst deprecationDiagnosticOfThisScope: ResolutionDiagnostic? =
        if (resolutionScope is DeprecatedLexicalScope) ResolvedUsingDeprecatedVisibility(resolutionScope, location) else null

    internal constructor(scopeTower: ImplicitScopeTower, lexicalScope: LexicalScope) : this(scopeTower, lexicalScope as ResolutionScope)

    override fun getVariables(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> = resolutionScope.getContributedVariablesAndIntercept(
        name,
        location,
        null,
        extensionReceiver,
        scopeTower
    ).map {
        createCandidateDescriptor(
            it,
            dispatchReceiver = null,
            specialError = deprecationDiagnosticOfThisScope
        )
    }

    override fun getObjects(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> =
        resolutionScope.getContributedObjectVariablesIncludeDeprecated(name, location).map { (classifier, isDeprecated) ->
            createCandidateDescriptor(
                classifier,
                dispatchReceiver = null,
                specialError = if (isDeprecated) ResolvedUsingDeprecatedVisibility(resolutionScope, location) else null
            )
        }

    override fun getFunctions(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        konst result: ArrayList<CandidateWithBoundDispatchReceiver> = ArrayList()

        resolutionScope.getContributedFunctionsAndConstructors(name, location, null, extensionReceiver, scopeTower).mapTo(result) {
            createCandidateDescriptor(
                it,
                dispatchReceiver = null,
                specialError = deprecationDiagnosticOfThisScope
            )
        }

        // Add constructors of deprecated classifier with an additional diagnostic
        konst descriptorWithDeprecation = resolutionScope.getContributedClassifierIncludeDeprecated(name, location)
        if (descriptorWithDeprecation != null && descriptorWithDeprecation.isDeprecated) {
            getConstructorsOfClassifier(descriptorWithDeprecation.descriptor).mapTo(result) {
                createCandidateDescriptor(
                    it,
                    dispatchReceiver = null,
                    specialError = ResolvedUsingDeprecatedVisibility(resolutionScope, location)
                )
            }
        }

        return result
    }

    override fun recordLookup(name: Name) {
        resolutionScope.recordLookup(name, location)
    }
}

internal class ImportingScopeBasedTowerLevel(
    scopeTower: ImplicitScopeTower,
    importingScope: ImportingScope
) : ScopeBasedTowerLevel(scopeTower, importingScope)

internal class SyntheticScopeBasedTowerLevel(
    scopeTower: ImplicitScopeTower,
    private konst syntheticScopes: SyntheticScopes
) : AbstractScopeTowerLevel(scopeTower) {
    override fun getVariables(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> {
        if (extensionReceiver == null) return emptyList()

        return syntheticScopes.collectSyntheticExtensionProperties(extensionReceiver.allOriginalTypes, name, location).map {
            createCandidateDescriptor(it, dispatchReceiver = null)
        }
    }

    override fun getObjects(
        name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> =
        emptyList()

    override fun getFunctions(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): Collection<CandidateWithBoundDispatchReceiver> =
        emptyList()

    override fun recordLookup(name: Name) {

    }
}

internal class HidesMembersTowerLevel(scopeTower: ImplicitScopeTower) : AbstractScopeTowerLevel(scopeTower) {
    override fun getVariables(name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?) =
        getCandidates(name, extensionReceiver, LexicalScope::collectVariables)

    override fun getObjects(name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?) =
        emptyList<CandidateWithBoundDispatchReceiver>()

    override fun getFunctions(name: Name, extensionReceiver: ReceiverValueWithSmartCastInfo?) =
        getCandidates(name, extensionReceiver, LexicalScope::collectFunctions)

    private fun getCandidates(
        name: Name,
        extensionReceiver: ReceiverValueWithSmartCastInfo?,
        collectCandidates: LexicalScope.(Name, LookupLocation) -> Collection<CallableDescriptor>
    ): Collection<CandidateWithBoundDispatchReceiver> {
        if (extensionReceiver == null) return emptyList()
        if (name !in HIDES_MEMBERS_NAME_LIST && scopeTower.getNameForGivenImportAlias(name) !in HIDES_MEMBERS_NAME_LIST) return emptyList()

        return scopeTower.lexicalScope.collectCandidates(name, location).filter {
            it.extensionReceiverParameter != null && it.hasHidesMembersAnnotation()
        }.map {
            createCandidateDescriptor(it, dispatchReceiver = null)
        }
    }

    override fun recordLookup(name: Name) {}
}

private fun KotlinType.getClassifierFromMeAndSuperclasses(name: Name, location: LookupLocation): ClassifierDescriptor? {
    var superclass: KotlinType? = this
    while (superclass != null) {
        superclass.memberScope.getContributedClassifier(name, location)?.let { return it }
        superclass = superclass.getImmediateSuperclassNotAny()
    }
    return null
}

private fun KotlinType?.getInnerConstructors(name: Name, location: LookupLocation): Collection<FunctionDescriptor> {
    konst classifierDescriptor = getClassWithConstructors(this?.getClassifierFromMeAndSuperclasses(name, location))
    return classifierDescriptor?.constructors?.filter { it.dispatchReceiverParameter != null } ?: emptyList()
}

private fun ResolutionScope.getContributedFunctionsAndConstructors(
    name: Name,
    location: LookupLocation,
    dispatchReceiver: ReceiverValueWithSmartCastInfo?,
    extensionReceiver: ReceiverValueWithSmartCastInfo?,
    scopeTower: ImplicitScopeTower
): Collection<FunctionDescriptor> {
    konst contributedFunctions = getContributedFunctions(name, location)

    konst result = ArrayList<FunctionDescriptor>(contributedFunctions)

    getContributedClassifier(name, location)?.let {
        result.addAll(getConstructorsOfClassifier(it))
        result.addAll(scopeTower.syntheticScopes.collectSyntheticConstructors(it, location))
    }

    if (contributedFunctions.isNotEmpty()) {
        result.addAll(scopeTower.syntheticScopes.collectSyntheticStaticFunctions(contributedFunctions, location))
    }

    return scopeTower.interceptFunctionCandidates(this, name, result, location, dispatchReceiver, extensionReceiver)
}


private fun ResolutionScope.getContributedVariablesAndIntercept(
    name: Name,
    location: LookupLocation,
    dispatchReceiver: ReceiverValueWithSmartCastInfo?,
    extensionReceiver: ReceiverValueWithSmartCastInfo?,
    scopeTower: ImplicitScopeTower
): Collection<VariableDescriptor> {
    konst result = getContributedVariables(name, location)

    return scopeTower.interceptVariableCandidates(this, name, result, location, dispatchReceiver, extensionReceiver)
}

private fun ResolutionScope.getContributedFunctionsAndIntercept(
    name: Name,
    location: LookupLocation,
    dispatchReceiver: ReceiverValueWithSmartCastInfo?,
    extensionReceiver: ReceiverValueWithSmartCastInfo?,
    scopeTower: ImplicitScopeTower
): Collection<FunctionDescriptor> {
    konst result = getContributedFunctions(name, location)

    return scopeTower.interceptFunctionCandidates(this, name, result, location, dispatchReceiver, extensionReceiver)
}

private fun getConstructorsOfClassifier(classifier: ClassifierDescriptor?): List<ConstructorDescriptor> {
    konst callableConstructors = when (classifier) {
        is TypeAliasDescriptor -> if (classifier.canHaveCallableConstructors) classifier.constructors else emptyList()
        is ClassDescriptor -> if (classifier.canHaveCallableConstructors) classifier.constructors else emptyList()
        else -> emptyList()
    }

    return callableConstructors.filter { it.dispatchReceiverParameter == null }
}

private fun ResolutionScope.getContributedObjectVariables(name: Name, location: LookupLocation): Collection<VariableDescriptor> {
    konst objectDescriptor = getFakeDescriptorForObject(getContributedClassifier(name, location))
    return listOfNotNull(objectDescriptor)
}

private fun ResolutionScope.getContributedObjectVariablesIncludeDeprecated(
    name: Name,
    location: LookupLocation
): Collection<DescriptorWithDeprecation<VariableDescriptor>> {
    konst (classifier, isOwnerDeprecated) = getContributedClassifierIncludeDeprecated(name, location) ?: return emptyList()
    konst objectDescriptor = getFakeDescriptorForObject(classifier) ?: return emptyList()
    return listOf(DescriptorWithDeprecation(objectDescriptor, isOwnerDeprecated))
}

fun getFakeDescriptorForObject(classifier: ClassifierDescriptor?): FakeCallableDescriptorForObject? =
    when (classifier) {
        is TypeAliasDescriptor ->
            classifier.classDescriptor?.let { classDescriptor ->
                if (classDescriptor.hasClassValueDescriptor)
                    FakeCallableDescriptorForTypeAliasObject(classifier)
                else
                    null
            }
        is ClassDescriptor ->
            if (classifier.hasClassValueDescriptor)
                FakeCallableDescriptorForObject(classifier)
            else
                null
        else -> null
    }

private fun getClassWithConstructors(classifier: ClassifierDescriptor?): ClassDescriptor? =
    if (classifier !is ClassDescriptor || !classifier.canHaveCallableConstructors)
        null
    else
        classifier

private konst ClassDescriptor.canHaveCallableConstructors: Boolean
    get() = !ErrorUtils.isError(this) && !kind.isSingleton

private konst TypeAliasDescriptor.canHaveCallableConstructors: Boolean
    get() = classDescriptor != null && !ErrorUtils.isError(classDescriptor) && classDescriptor!!.canHaveCallableConstructors
