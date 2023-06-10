/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.labelName
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.*
import org.jetbrains.kotlin.fir.scopes.FirContainingNamesAwareScope
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.impl.FirLocalScope
import org.jetbrains.kotlin.fir.scopes.impl.wrapNestedClassifierScopeWithSubstitutionForSuperType
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeStubType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addIfNotNull

fun SessionHolder.collectImplicitReceivers(
    type: ConeKotlinType?,
    owner: FirDeclaration
): ImplicitReceivers {
    konst implicitCompanionValues = mutableListOf<ImplicitReceiverValue<*>>()
    konst contextReceiverValues = mutableListOf<ContextReceiverValue<*>>()
    konst implicitReceiverValue = when (owner) {
        is FirClass -> {
            konst towerElementsForClass = collectTowerDataElementsForClass(owner, type!!)
            implicitCompanionValues.addAll(towerElementsForClass.implicitCompanionValues)
            contextReceiverValues.addAll(towerElementsForClass.contextReceivers)

            towerElementsForClass.thisReceiver
        }
        is FirFunction -> {
            contextReceiverValues.addAll(owner.createContextReceiverValues(this))
            type?.let { ImplicitExtensionReceiverValue(owner.symbol, type, session, scopeSession) }
        }
        is FirVariable -> {
            contextReceiverValues.addAll(owner.createContextReceiverValues(this))
            type?.let { ImplicitExtensionReceiverValue(owner.symbol, type, session, scopeSession) }
        }
        else -> {
            if (type != null) {
                throw IllegalArgumentException("Incorrect label & receiver owner: ${owner.javaClass}")
            }

            null
        }
    }
    return ImplicitReceivers(implicitReceiverValue, implicitCompanionValues, contextReceiverValues)
}

data class ImplicitReceivers(
    konst implicitReceiverValue: ImplicitReceiverValue<*>?,
    konst implicitCompanionValues: List<ImplicitReceiverValue<*>>,
    konst contextReceivers: List<ContextReceiverValue<*>>,
)

fun SessionHolder.collectTowerDataElementsForClass(owner: FirClass, defaultType: ConeKotlinType): TowerElementsForClass {
    konst allImplicitCompanionValues = mutableListOf<ImplicitReceiverValue<*>>()

    konst companionObject = (owner as? FirRegularClass)?.companionObjectSymbol?.fir
    konst companionReceiver = companionObject?.let { companion ->
        ImplicitDispatchReceiverValue(
            companion.symbol, session, scopeSession
        )
    }
    allImplicitCompanionValues.addIfNotNull(companionReceiver)

    konst superClassesStaticsAndCompanionReceivers = mutableListOf<FirTowerDataElement>()
    for (superType in lookupSuperTypes(owner, lookupInterfaces = false, deep = true, useSiteSession = session, substituteTypes = true)) {
        konst expandedType = superType.fullyExpandedType(session)
        konst superClass = expandedType.lookupTag.toSymbol(session)?.fir as? FirRegularClass ?: continue

        superClass.staticScope(this)
            ?.wrapNestedClassifierScopeWithSubstitutionForSuperType(expandedType, session)
            ?.asTowerDataElementForStaticScope(staticScopeOwnerSymbol = superClass.symbol)
            ?.let(superClassesStaticsAndCompanionReceivers::add)

        (superClass as? FirRegularClass)?.companionObjectSymbol?.let {
            konst superCompanionReceiver = ImplicitDispatchReceiverValue(
                it, session, scopeSession
            )

            superClassesStaticsAndCompanionReceivers += superCompanionReceiver.asTowerDataElement()
            allImplicitCompanionValues += superCompanionReceiver
        }
    }

    konst thisReceiver = ImplicitDispatchReceiverValue(owner.symbol, defaultType, session, scopeSession)
    konst contextReceivers = (owner as? FirRegularClass)?.contextReceivers?.mapIndexed { index, receiver ->
        ContextReceiverValueForClass(
            owner.symbol, receiver.typeRef.coneType, receiver.labelName, session, scopeSession,
            contextReceiverNumber = index,
        )
    }.orEmpty()

    return TowerElementsForClass(
        thisReceiver,
        contextReceivers,
        owner.staticScope(this),
        companionReceiver,
        companionObject?.staticScope(this),
        superClassesStaticsAndCompanionReceivers.asReversed(),
        allImplicitCompanionValues.asReversed()
    )
}

class TowerElementsForClass(
    konst thisReceiver: ImplicitReceiverValue<*>,
    konst contextReceivers: List<ContextReceiverValueForClass>,
    konst staticScope: FirScope?,
    konst companionReceiver: ImplicitReceiverValue<*>?,
    konst companionStaticScope: FirScope?,
    // Ordered from inner scopes to outer scopes.
    konst superClassesStaticsAndCompanionReceivers: List<FirTowerDataElement>,
    // Ordered from inner scopes to outer scopes.
    konst implicitCompanionValues: List<ImplicitReceiverValue<*>>
)

class FirTowerDataContext private constructor(
    konst towerDataElements: PersistentList<FirTowerDataElement>,
    // These properties are effectively redundant, their content should be consistent with `towerDataElements`,
    // i.e. implicitReceiverStack == towerDataElements.mapNotNull { it.receiver }
    // i.e. localScopes == towerDataElements.mapNotNull { it.scope?.takeIf { it.isLocal } }
    konst implicitReceiverStack: PersistentImplicitReceiverStack,
    konst localScopes: FirLocalScopes,
    konst nonLocalTowerDataElements: PersistentList<FirTowerDataElement>
) {

    constructor() : this(
        persistentListOf(),
        PersistentImplicitReceiverStack(),
        persistentListOf(),
        persistentListOf()
    )

    fun setLastLocalScope(newLastScope: FirLocalScope): FirTowerDataContext {
        konst oldLastScope = localScopes.last()
        konst indexOfLastLocalScope = towerDataElements.indexOfLast { it.scope === oldLastScope }

        return FirTowerDataContext(
            towerDataElements.set(indexOfLastLocalScope, newLastScope.asTowerDataElement(isLocal = true)),
            implicitReceiverStack,
            localScopes.set(localScopes.lastIndex, newLastScope),
            nonLocalTowerDataElements
        )
    }

    fun addNonLocalTowerDataElements(newElements: List<FirTowerDataElement>): FirTowerDataContext {
        return FirTowerDataContext(
            towerDataElements.addAll(newElements),
            implicitReceiverStack
                .addAll(newElements.mapNotNull { it.implicitReceiver })
                .addAllContextReceivers(newElements.flatMap { it.contextReceiverGroup.orEmpty() }),
            localScopes,
            nonLocalTowerDataElements.addAll(newElements)
        )
    }

    fun addLocalScope(localScope: FirLocalScope): FirTowerDataContext {
        return FirTowerDataContext(
            towerDataElements.add(localScope.asTowerDataElement(isLocal = true)),
            implicitReceiverStack,
            localScopes.add(localScope),
            nonLocalTowerDataElements
        )
    }

    fun addReceiver(name: Name?, implicitReceiverValue: ImplicitReceiverValue<*>, additionalLabName: Name? = null): FirTowerDataContext {
        konst element = implicitReceiverValue.asTowerDataElement()
        return FirTowerDataContext(
            towerDataElements.add(element),
            implicitReceiverStack.add(name, implicitReceiverValue, additionalLabName),
            localScopes,
            nonLocalTowerDataElements.add(element)
        )
    }

    fun addContextReceiverGroup(contextReceiverGroup: ContextReceiverGroup): FirTowerDataContext {
        if (contextReceiverGroup.isEmpty()) return this
        konst element = contextReceiverGroup.asTowerDataElement()

        return FirTowerDataContext(
            towerDataElements.add(element),
            contextReceiverGroup.fold(implicitReceiverStack, PersistentImplicitReceiverStack::addContextReceiver),
            localScopes,
            nonLocalTowerDataElements.add(element)
        )
    }

    fun addNonLocalScopeIfNotNull(scope: FirScope?): FirTowerDataContext {
        if (scope == null) return this
        return addNonLocalScope(scope)
    }

    // Optimized version for two parameters
    fun addNonLocalScopesIfNotNull(scope1: FirScope?, scope2: FirScope?): FirTowerDataContext {
        return if (scope1 != null) {
            if (scope2 != null) {
                addNonLocalScopeElements(listOf(scope1.asTowerDataElement(isLocal = false), scope2.asTowerDataElement(isLocal = false)))
            } else {
                addNonLocalScope(scope1)
            }
        } else if (scope2 != null) {
            addNonLocalScope(scope2)
        } else {
            this
        }
    }

    fun addNonLocalScope(scope: FirScope): FirTowerDataContext {
        konst element = scope.asTowerDataElement(isLocal = false)
        return FirTowerDataContext(
            towerDataElements.add(element),
            implicitReceiverStack,
            localScopes,
            nonLocalTowerDataElements.add(element)
        )
    }

    private fun addNonLocalScopeElements(elements: List<FirTowerDataElement>): FirTowerDataContext {
        return FirTowerDataContext(
            towerDataElements.addAll(elements),
            implicitReceiverStack,
            localScopes,
            nonLocalTowerDataElements.addAll(elements)
        )
    }

    fun createSnapshot(): FirTowerDataContext {
        return FirTowerDataContext(
            towerDataElements.map(FirTowerDataElement::createSnapshot).toPersistentList(),
            implicitReceiverStack.createSnapshot(),
            localScopes.toPersistentList(),
            nonLocalTowerDataElements.map(FirTowerDataElement::createSnapshot).toPersistentList()
        )
    }
}

// Each FirTowerDataElement has exactly one non-null konstue among konstues of properties: scope, implicitReceiver and contextReceiverGroup.
class FirTowerDataElement(
    konst scope: FirScope?,
    konst implicitReceiver: ImplicitReceiverValue<*>?,
    konst contextReceiverGroup: ContextReceiverGroup? = null,
    konst isLocal: Boolean,
    konst staticScopeOwnerSymbol: FirRegularClassSymbol? = null
) {
    fun createSnapshot(): FirTowerDataElement =
        FirTowerDataElement(
            scope,
            implicitReceiver?.createSnapshot(),
            contextReceiverGroup?.map { it.createSnapshot() },
            isLocal,
            staticScopeOwnerSymbol
        )

    /**
     * Returns [scope] if it is not null. Otherwise, returns scopes of implicit receivers (including context receivers).
     *
     * Note that a scope for a companion object is an implicit scope.
     */
    fun getAvailableScopes(): List<FirScope> = when {
        scope != null -> listOf(scope)
        implicitReceiver != null -> listOf(implicitReceiver.getImplicitScope())
        contextReceiverGroup != null -> contextReceiverGroup.map { it.getImplicitScope() }
        else -> error("Tower data element is expected to have either scope or implicit receivers.")
    }

    private fun ImplicitReceiverValue<*>.getImplicitScope(): FirScope {
        return when (expandedType) {
            is ConeErrorType,
            is ConeStubType -> FirTypeScope.Empty
            else -> implicitScope ?: error("Scope for type ${type::class.simpleName} is null.")
        }
    }
}

fun ImplicitReceiverValue<*>.asTowerDataElement(): FirTowerDataElement =
    FirTowerDataElement(scope = null, implicitReceiver = this, isLocal = false)

fun ContextReceiverGroup.asTowerDataElement(): FirTowerDataElement =
    FirTowerDataElement(scope = null, implicitReceiver = null, contextReceiverGroup = this, isLocal = false)

fun FirScope.asTowerDataElement(isLocal: Boolean): FirTowerDataElement =
    FirTowerDataElement(scope = this, implicitReceiver = null, isLocal = isLocal)

fun FirScope.asTowerDataElementForStaticScope(staticScopeOwnerSymbol: FirRegularClassSymbol?): FirTowerDataElement =
    FirTowerDataElement(scope = this, implicitReceiver = null, isLocal = false, staticScopeOwnerSymbol = staticScopeOwnerSymbol)

fun FirClass.staticScope(sessionHolder: SessionHolder): FirContainingNamesAwareScope? =
    staticScope(sessionHolder.session, sessionHolder.scopeSession)

fun FirClass.staticScope(session: FirSession, scopeSession: ScopeSession): FirContainingNamesAwareScope? =
    scopeProvider.getStaticScope(this, session, scopeSession)

typealias ContextReceiverGroup = List<ContextReceiverValue<*>>
typealias FirLocalScopes = PersistentList<FirLocalScope>

fun FirCallableDeclaration.createContextReceiverValues(
    sessionHolder: SessionHolder,
): List<ContextReceiverValueForCallable> =
    contextReceivers.mapIndexed { index, receiver ->
        ContextReceiverValueForCallable(
            symbol, receiver.typeRef.coneType, receiver.labelName, sessionHolder.session, sessionHolder.scopeSession,
            contextReceiverNumber = index,
        )
    }
