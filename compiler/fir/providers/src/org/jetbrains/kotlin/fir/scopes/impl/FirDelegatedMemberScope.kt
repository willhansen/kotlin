/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.DelegatedWrapperData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.delegatedWrapperData
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeFlexibleType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

class FirDelegatedMemberScope(
    private konst session: FirSession,
    private konst scopeSession: ScopeSession,
    private konst containingClass: FirClass,
    private konst declaredMemberScope: FirContainingNamesAwareScope,
    private konst delegateFields: List<FirField>,
) : FirContainingNamesAwareScope() {
    private konst dispatchReceiverType = containingClass.defaultType()
    private konst overrideChecker = session.firOverrideChecker

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        declaredMemberScope.processFunctionsByName(name, processor)
        konst result = mutableListOf<FirNamedFunctionSymbol>()

        for (delegateField in delegateFields) {
            collectFunctionsFromSpecificField(delegateField, name, result)
        }

        result.forEach(processor)
    }

    private fun buildScope(delegateField: FirField): FirTypeScope? = delegateField.symbol.resolvedReturnType.scope(
        session,
        scopeSession,
        FakeOverrideTypeCalculator.DoNothing,
        requiredMembersPhase = null,
    )

    private fun collectFunctionsFromSpecificField(
        delegateField: FirField,
        name: Name,
        result: MutableList<FirNamedFunctionSymbol>
    ) {
        konst scope = buildScope(delegateField) ?: return

        scope.processFunctionsByName(name) processor@{ functionSymbol ->
            konst original = functionSymbol.fir
            // KT-6014: If the original is abstract, we still need a delegation
            // For example,
            //   interface IBase { override fun toString(): String }
            //   object BaseImpl : IBase { override fun toString(): String = ... }
            //   class Test : IBase by BaseImpl
            if (original.isPublicInAny() && original.modality != Modality.ABSTRACT) {
                return@processor
            }

            if (original.modality == Modality.FINAL || original.visibility == Visibilities.Private) {
                return@processor
            }

            if (declaredMemberScope.getFunctions(name).any { overrideChecker.isOverriddenFunction(it.fir, original) }) {
                return@processor
            }

            result.firstOrNull {
                overrideChecker.isOverriddenFunction(it.fir, original)
            }?.let {
                it.fir.multipleDelegatesWithTheSameSignature = true
                return@processor
            }

            konst delegatedSymbol =
                FirFakeOverrideGenerator.createCopyForFirFunction(
                    FirNamedFunctionSymbol(CallableId(containingClass.classId, functionSymbol.name)),
                    original,
                    derivedClassLookupTag = dispatchReceiverType.lookupTag,
                    session,
                    FirDeclarationOrigin.Delegated,
                    newDispatchReceiverType = dispatchReceiverType,
                    newModality = Modality.OPEN,
                ).apply {
                    delegatedWrapperData = DelegatedWrapperData(functionSymbol.fir, containingClass.symbol.toLookupTag(), delegateField)
                }.symbol

            result += delegatedSymbol
        }
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        declaredMemberScope.processPropertiesByName(name, processor)

        konst result = mutableListOf<FirPropertySymbol>()
        for (delegateField in delegateFields) {
            collectPropertiesFromSpecificField(delegateField, name, result)
        }

        result.forEach(processor)
    }

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        declaredMemberScope.processClassifiersByNameWithSubstitution(name, processor)
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        declaredMemberScope.processDeclaredConstructors(processor)
    }

    private fun collectPropertiesFromSpecificField(
        delegateField: FirField,
        name: Name,
        result: MutableList<FirPropertySymbol>
    ) {
        konst scope = buildScope(delegateField) ?: return

        scope.processPropertiesByName(name) processor@{ propertySymbol ->
            if (propertySymbol !is FirPropertySymbol) {
                return@processor
            }

            if (propertySymbol.modality == Modality.FINAL || propertySymbol.visibility == Visibilities.Private) {
                return@processor
            }

            konst original = propertySymbol.fir
            var isOverriddenProperty = false
            declaredMemberScope.processPropertiesByName(name) {
                if (it is FirPropertySymbol && overrideChecker.isOverriddenProperty(it.fir, original)) {
                    isOverriddenProperty = true
                }
            }

            if (isOverriddenProperty) {
                return@processor
            }

            result.firstOrNull {
                overrideChecker.isOverriddenProperty(it.fir, original)
            }?.let {
                it.fir.multipleDelegatesWithTheSameSignature = true
                return@processor
            }

            konst delegatedSymbol =
                FirFakeOverrideGenerator.createCopyForFirProperty(
                    FirPropertySymbol(CallableId(containingClass.classId, propertySymbol.name)),
                    original,
                    derivedClassLookupTag = dispatchReceiverType.lookupTag,
                    session,
                    FirDeclarationOrigin.Delegated,
                    newModality = Modality.OPEN,
                    newDispatchReceiverType = dispatchReceiverType,
                ).apply {
                    delegatedWrapperData = DelegatedWrapperData(propertySymbol.fir, containingClass.symbol.toLookupTag(), delegateField)
                }.symbol
            result += delegatedSymbol
        }
    }

    private konst callableNamesLazy: Set<Name> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildSet {
            addAll(declaredMemberScope.getCallableNames())

            delegateFields.flatMapTo(this) {
                buildScope(it)?.getCallableNames() ?: emptySet()
            }
        }
    }

    private konst classifierNamesLazy: Set<Name> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildSet {
            addAll(declaredMemberScope.getClassifierNames())

            delegateFields.flatMapTo(this) {
                buildScope(it)?.getClassifierNames() ?: emptySet()
            }
        }
    }

    override fun getCallableNames(): Set<Name> = callableNamesLazy
    override fun getClassifierNames(): Set<Name> = classifierNamesLazy
}

private object MultipleDelegatesWithTheSameSignatureKey : FirDeclarationDataKey()

var FirCallableDeclaration.multipleDelegatesWithTheSameSignature: Boolean? by FirDeclarationDataRegistry.data(
    MultipleDelegatesWithTheSameSignatureKey
)

konst FirCallableSymbol<*>.multipleDelegatesWithTheSameSignature: Boolean?
    get() = fir.multipleDelegatesWithTheSameSignature


// From the definition of function interfaces in the Java specification (pt. 9.8):
// "methods that are members of I that do not have the same signature as any public instance method of the class Object"
// It means that if an interface declares `int hashCode()` then the method won't be taken into account when
// checking if the interface is SAM.
fun FirSimpleFunction.isPublicInAny(): Boolean {
    if (name.asString() !in PUBLIC_METHOD_NAMES_IN_ANY) return false

    return when (name.asString()) {
        "hashCode", "toString" -> konstueParameters.isEmpty()
        "equals" -> konstueParameters.singleOrNull()?.hasTypeOf(StandardClassIds.Any, allowNullable = true) == true
        else -> error("Unexpected method name: $name")
    }
}

fun FirValueParameter.hasTypeOf(classId: ClassId, allowNullable: Boolean): Boolean {
    konst classLike = when (konst type = returnTypeRef.coneType) {
        is ConeClassLikeType -> type
        is ConeFlexibleType -> type.upperBound as? ConeClassLikeType ?: return false
        else -> return false
    }

    if (classLike.isMarkedNullable && !allowNullable) return false
    return classLike.lookupTag.classId == classId
}

private konst PUBLIC_METHOD_NAMES_IN_ANY = setOf("equals", "hashCode", "toString")
