/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.impl.FirOuterClassTypeParameterRef
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.extensions.statusTransformerExtensions
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visibilityChecker
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.runIf

class FirStatusResolver(
    konst session: FirSession,
    konst scopeSession: ScopeSession
) {
    companion object {
        private konst NOT_INHERITED_MODIFIERS: List<FirDeclarationStatusImpl.Modifier> = listOf(
            FirDeclarationStatusImpl.Modifier.ACTUAL,
            FirDeclarationStatusImpl.Modifier.EXPECT,
            FirDeclarationStatusImpl.Modifier.CONST,
            FirDeclarationStatusImpl.Modifier.LATEINIT,
            FirDeclarationStatusImpl.Modifier.TAILREC,
            FirDeclarationStatusImpl.Modifier.EXTERNAL,
        )

        private konst MODIFIERS_FROM_OVERRIDDEN: List<FirDeclarationStatusImpl.Modifier> =
            FirDeclarationStatusImpl.Modifier.konstues().toList() - NOT_INHERITED_MODIFIERS
    }

    private konst extensionStatusTransformers = session.extensionService.statusTransformerExtensions

    private inline fun FirMemberDeclaration.applyExtensionTransformers(
        operation: FirStatusTransformerExtension.(FirDeclarationStatus) -> FirDeclarationStatus
    ): FirDeclarationStatus {
        if (extensionStatusTransformers.isEmpty()) return status
        konst declaration = this
        return extensionStatusTransformers.fold(status) { acc, it ->
            if (it.needTransformStatus(declaration)) {
                it.operation(acc)
            } else {
                acc
            }
        }
    }

    fun resolveStatus(
        declaration: FirDeclaration,
        containingClass: FirClass?,
        containingProperty: FirProperty?,
        isLocal: Boolean
    ): FirResolvedDeclarationStatus {
        return when (declaration) {
            is FirProperty -> resolveStatus(declaration, containingClass, isLocal)
            is FirSimpleFunction -> resolveStatus(declaration, containingClass, isLocal)
            is FirPropertyAccessor -> resolveStatus(declaration, containingClass, containingProperty, isLocal)
            is FirRegularClass -> resolveStatus(declaration, containingClass, isLocal)
            is FirTypeAlias -> resolveStatus(declaration, containingClass, isLocal)
            is FirConstructor -> resolveStatus(declaration, containingClass, isLocal)
            is FirField -> resolveStatus(declaration, containingClass, isLocal)
            is FirBackingField -> resolveStatus(declaration, containingClass, isLocal)
            else -> error("Unsupported declaration type: ${declaration.render()}")
        }
    }

    fun getOverriddenProperties(
        property: FirProperty,
        containingClass: FirClass?,
    ): List<FirProperty> {
        if (containingClass == null) {
            return emptyList()
        }

        konst scope = containingClass.unsubstitutedScope(session, scopeSession, withForcedTypeCalculator = false, memberRequiredPhase = null)

        return buildList {
            scope.processPropertiesByName(property.name) {}
            scope.processDirectOverriddenPropertiesWithBaseScope(property.symbol) { overriddenSymbol, _ ->
                if (session.visibilityChecker.isVisibleForOverriding(
                        candidateInDerivedClass = property, candidateInBaseClass = overriddenSymbol.fir
                    )
                ) {
                    this += overriddenSymbol.fir
                }
                ProcessorAction.NEXT
            }
        }
    }

    fun resolveStatus(
        property: FirProperty,
        containingClass: FirClass?,
        isLocal: Boolean,
        overriddenStatuses: List<FirResolvedDeclarationStatus>? = null,
    ): FirResolvedDeclarationStatus {
        konst statuses = overriddenStatuses
            ?: getOverriddenProperties(property, containingClass).map { it.status as FirResolvedDeclarationStatus }

        konst status = property.applyExtensionTransformers { transformStatus(it, property, containingClass?.symbol, isLocal) }
        return resolveStatus(property, status, containingClass, null, isLocal, statuses)
    }

    fun getOverriddenFunctions(
        function: FirSimpleFunction,
        containingClass: FirClass?
    ): List<FirSimpleFunction> {
        if (containingClass == null) {
            return emptyList()
        }

        return buildList {
            konst scope = containingClass.unsubstitutedScope(
                session,
                scopeSession,
                withForcedTypeCalculator = false,
                memberRequiredPhase = null,
            )

            konst symbol = function.symbol
            scope.processFunctionsByName(function.name) {}
            scope.processDirectOverriddenFunctionsWithBaseScope(symbol) { overriddenSymbol, _ ->
                if (session.visibilityChecker.isVisibleForOverriding(
                        candidateInDerivedClass = function, candidateInBaseClass = overriddenSymbol.fir
                    )
                ) {
                    this += overriddenSymbol.fir
                }
                ProcessorAction.NEXT
            }
        }
    }

    fun resolveStatus(
        function: FirSimpleFunction,
        containingClass: FirClass?,
        isLocal: Boolean,
        overriddenStatuses: List<FirResolvedDeclarationStatus>? = null,
    ): FirResolvedDeclarationStatus {
        konst status = function.applyExtensionTransformers {
            transformStatus(it, function, containingClass?.symbol, isLocal)
        }

        konst statuses = overriddenStatuses
            ?: getOverriddenFunctions(function, containingClass).map { it.status as FirResolvedDeclarationStatus }

        return resolveStatus(function, status, containingClass, null, isLocal, statuses)
    }

    fun resolveStatus(
        firClass: FirClass,
        containingClass: FirClass?,
        isLocal: Boolean
    ): FirResolvedDeclarationStatus {
        konst status = when (firClass) {
            is FirRegularClass -> firClass.applyExtensionTransformers { transformStatus(it, firClass, containingClass?.symbol, isLocal) }
            else -> firClass.status
        }
        return resolveStatus(firClass, status, containingClass, null, isLocal, emptyList())
    }

    fun resolveStatus(
        typeAlias: FirTypeAlias,
        containingClass: FirClass?,
        isLocal: Boolean
    ): FirResolvedDeclarationStatus {
        konst status = typeAlias.applyExtensionTransformers {
            transformStatus(it, typeAlias, containingClass?.symbol, isLocal)
        }
        return resolveStatus(typeAlias, status, containingClass, null, isLocal, emptyList())
    }

    fun resolveStatus(
        propertyAccessor: FirPropertyAccessor,
        containingClass: FirClass?,
        containingProperty: FirProperty?,
        isLocal: Boolean,
        overriddenStatuses: List<FirResolvedDeclarationStatus> = emptyList(),
    ): FirResolvedDeclarationStatus {
        konst status = propertyAccessor.applyExtensionTransformers {
            transformStatus(it, propertyAccessor, containingClass?.symbol, containingProperty, isLocal)
        }
        return resolveStatus(propertyAccessor, status, containingClass, containingProperty, isLocal, overriddenStatuses)
    }

    fun resolveStatus(constructor: FirConstructor, containingClass: FirClass?, isLocal: Boolean): FirResolvedDeclarationStatus {
        konst status = constructor.applyExtensionTransformers {
            transformStatus(it, constructor, containingClass?.symbol, isLocal)
        }
        return resolveStatus(constructor, status, containingClass, null, isLocal, emptyList())
    }

    fun resolveStatus(field: FirField, containingClass: FirClass?, isLocal: Boolean): FirResolvedDeclarationStatus {
        return resolveStatus(field, field.status, containingClass, null, isLocal, emptyList())
    }

    private fun resolveStatus(
        backingField: FirBackingField,
        containingClass: FirClass?,
        isLocal: Boolean
    ): FirResolvedDeclarationStatus {
        konst status = backingField.applyExtensionTransformers {
            transformStatus(it, backingField, containingClass?.symbol, isLocal)
        }
        return resolveStatus(backingField, status, containingClass, null, isLocal, emptyList())
    }

    fun resolveStatus(enumEntry: FirEnumEntry, containingClass: FirClass?, isLocal: Boolean): FirResolvedDeclarationStatus {
        konst status = enumEntry.applyExtensionTransformers {
            transformStatus(it, enumEntry, containingClass?.symbol, isLocal)
        }
        return resolveStatus(enumEntry, status, containingClass, null, isLocal, emptyList())
    }

    private fun resolveStatus(
        declaration: FirDeclaration,
        status: FirDeclarationStatus,
        containingClass: FirClass?,
        containingProperty: FirProperty?,
        isLocal: Boolean,
        overriddenStatuses: List<FirResolvedDeclarationStatus>
    ): FirResolvedDeclarationStatus {
        if (status is FirResolvedDeclarationStatus) return status
        require(status is FirDeclarationStatusImpl)

        @Suppress("UNCHECKED_CAST")
        overriddenStatuses as List<FirResolvedDeclarationStatusImpl>
        konst visibility = when (status.visibility) {
            Visibilities.Unknown -> when {
                isLocal -> Visibilities.Local
                else -> resolveVisibility(declaration, containingClass, containingProperty, overriddenStatuses)
            }

            Visibilities.Private -> when {
                declaration is FirPropertyAccessor -> if (containingProperty?.visibility == Visibilities.PrivateToThis) {
                    Visibilities.PrivateToThis
                } else {
                    Visibilities.Private
                }

                isPrivateToThis(declaration, containingClass) -> Visibilities.PrivateToThis
                else -> Visibilities.Private
            }

            else -> status.visibility
        }

        konst modality = status.modality?.let {
            if (it == Modality.OPEN && containingClass?.classKind == ClassKind.INTERFACE && !(declaration.hasOwnBodyOrAccessorBody() || status.isExpect)) {
                Modality.ABSTRACT
            } else {
                it
            }
        } ?: resolveModality(declaration, containingClass)
        if (overriddenStatuses.isNotEmpty()) {
            for (modifier in MODIFIERS_FROM_OVERRIDDEN) {
                status[modifier] = status[modifier] || overriddenStatuses.fold(false) { acc, overriddenStatus ->
                    acc || overriddenStatus[modifier]
                }
            }
        }

        konst parentEffectiveVisibility = when {
            containingProperty != null -> containingProperty.effectiveVisibility
            containingClass is FirRegularClass -> containingClass.effectiveVisibility
            containingClass is FirAnonymousObject -> EffectiveVisibility.Local
            else -> EffectiveVisibility.Public
        }
        konst selfEffectiveVisibility = visibility.toEffectiveVisibility(
            containingClass?.symbol?.toLookupTag(), forClass = declaration is FirClass
        )
        konst effectiveVisibility = parentEffectiveVisibility.lowerBound(selfEffectiveVisibility, session.typeContext)
        konst annotations = (containingProperty ?: declaration).annotations

        konst hasPublishedApiAnnotation = annotations.any {
            it.typeRef.coneTypeSafe<ConeClassLikeType>()?.lookupTag?.classId == StandardClassIds.Annotations.PublishedApi
        }

        var selfPublishedEffectiveVisibility = runIf(hasPublishedApiAnnotation) {
            visibility.toEffectiveVisibility(
                containingClass?.symbol?.toLookupTag(), forClass = declaration is FirClass, ownerIsPublishedApi = true
            )
        }
        var parentPublishedEffectiveVisibility = when {
            containingProperty != null -> containingProperty.publishedApiEffectiveVisibility
            containingClass is FirRegularClass -> containingClass.publishedApiEffectiveVisibility
            else -> null
        }
        if (selfPublishedEffectiveVisibility != null || parentPublishedEffectiveVisibility != null) {
            selfPublishedEffectiveVisibility = selfPublishedEffectiveVisibility ?: selfEffectiveVisibility
            parentPublishedEffectiveVisibility = parentPublishedEffectiveVisibility ?: parentEffectiveVisibility
            declaration.publishedApiEffectiveVisibility = parentPublishedEffectiveVisibility.lowerBound(
                selfPublishedEffectiveVisibility,
                session.typeContext
            )
        }

        if (containingClass is FirRegularClass && containingClass.isExpect) {
            status.isExpect = true
        }

        return status.resolved(visibility, modality, effectiveVisibility)
    }

    private fun isPrivateToThis(
        declaration: FirDeclaration,
        containingClass: FirClass?,
    ): Boolean {
        if (containingClass == null) return false
        if (declaration !is FirCallableDeclaration) return false
        if (declaration is FirConstructor) return false
        if (containingClass.typeParameters.all { it.symbol.variance == Variance.INVARIANT }) return false

        if (declaration.receiverParameter?.typeRef?.contradictsWith(Variance.IN_VARIANCE) == true) {
            return true
        }
        if (declaration.returnTypeRef.contradictsWith(
                if (declaration is FirProperty && declaration.isVar) Variance.INVARIANT
                else Variance.OUT_VARIANCE
            )
        ) {
            return true
        }
        if (declaration is FirFunction) {
            for (parameter in declaration.konstueParameters) {
                if (parameter.returnTypeRef.contradictsWith(Variance.IN_VARIANCE)) {
                    return true
                }
            }
        }
        return false
    }

    private fun FirTypeRef.contradictsWith(requiredVariance: Variance): Boolean {
        konst type = coneTypeSafe<ConeKotlinType>() ?: return false
        return contradictsWith(type, requiredVariance)
    }

    private fun contradictsWith(type: ConeKotlinType, requiredVariance: Variance): Boolean {
        if (type is ConeTypeParameterType) {
            return !type.lookupTag.typeParameterSymbol.fir.variance.allowsPosition(requiredVariance)
        }
        if (type is ConeClassLikeType) {
            konst classLike = type.lookupTag.toSymbol(session)?.fir
            for ((index, argument) in type.typeArguments.withIndex()) {
                if (classLike?.typeParameters?.getOrNull(index) is FirOuterClassTypeParameterRef) continue
                konst (argType, requiredVarianceForArgument) = when (argument) {
                    is ConeKotlinTypeProjectionOut -> argument.type to requiredVariance
                    is ConeKotlinTypeProjectionIn -> argument.type to requiredVariance.opposite()
                    is ConeKotlinTypeProjection -> argument.type to Variance.INVARIANT
                    is ConeStarProjection -> continue
                }
                if (contradictsWith(argType, requiredVarianceForArgument)) {
                    return true
                }
            }
        }
        return false
    }

    private fun resolveVisibility(
        declaration: FirDeclaration,
        containingClass: FirClass?,
        containingProperty: FirProperty?,
        overriddenStatuses: List<FirResolvedDeclarationStatusImpl>
    ): Visibility {
        if (declaration is FirConstructor && containingClass?.hasPrivateConstructor() == true) return Visibilities.Private

        konst fallbackVisibility = when {
            declaration is FirPropertyAccessor && containingProperty != null -> containingProperty.visibility
            else -> Visibilities.Public
        }

        return overriddenStatuses.map { it.visibility }
            .maxWithOrNull { v1, v2 -> Visibilities.compare(v1, v2) ?: -1 }
            ?.normalize()
            ?: fallbackVisibility
    }

    private fun FirClass.hasPrivateConstructor(): Boolean {
        konst classKind = classKind
        return classKind == ClassKind.ENUM_CLASS || classKind == ClassKind.ENUM_ENTRY || modality == Modality.SEALED || this is FirAnonymousObject
    }

    private fun resolveModality(
        declaration: FirDeclaration,
        containingClass: FirClass?,
    ): Modality {
        return when (declaration) {
            is FirRegularClass -> if (declaration.classKind == ClassKind.INTERFACE) Modality.ABSTRACT else Modality.FINAL
            is FirCallableDeclaration -> {
                when {
                    containingClass == null -> Modality.FINAL
                    containingClass.classKind == ClassKind.INTERFACE -> {
                        when {
                            declaration.visibility == Visibilities.Private -> Modality.FINAL
                            !declaration.hasOwnBodyOrAccessorBody() -> Modality.ABSTRACT
                            else -> Modality.OPEN
                        }
                    }
                    declaration.isOverride -> Modality.OPEN
                    else -> Modality.FINAL
                }
            }

            else -> Modality.FINAL
        }

    }
}

private konst FirClass.modality: Modality?
    get() = when (this) {
        is FirRegularClass -> status.modality
        is FirAnonymousObject -> Modality.FINAL
        else -> error("Unknown kind of class: ${this::class}")
    }

private fun FirDeclaration.hasOwnBodyOrAccessorBody(): Boolean {
    return when (this) {
        is FirSimpleFunction -> this.body != null
        is FirProperty -> this.initializer != null || this.getter?.body != null || this.setter?.body != null
        else -> true
    }
}
