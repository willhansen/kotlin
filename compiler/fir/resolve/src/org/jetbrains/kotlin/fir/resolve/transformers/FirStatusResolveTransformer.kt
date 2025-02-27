/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.componentFunctionSymbol
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.LocalClassesNavigationInfo
import org.jetbrains.kotlin.fir.scopes.FirCompositeScope
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousObjectSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhaseWithCallableMembers
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.toSymbol
import org.jetbrains.kotlin.fir.visitors.transformSingle

class FirStatusResolveProcessor(
    session: FirSession,
    scopeSession: ScopeSession
) : FirTransformerBasedResolveProcessor(session, scopeSession, FirResolvePhase.STATUS) {
    override konst transformer = run {
        konst statusComputationSession = StatusComputationSession()
        FirStatusResolveTransformer(
            session,
            scopeSession,
            statusComputationSession
        )
    }
}

fun <F : FirClassLikeDeclaration> F.runStatusResolveForLocalClass(
    session: FirSession,
    scopeSession: ScopeSession,
    scopesForLocalClass: List<FirScope>,
    localClassesNavigationInfo: LocalClassesNavigationInfo
): F {
    konst statusComputationSession = StatusComputationSession()
    konst transformer = FirStatusResolveTransformer(
        session,
        scopeSession,
        statusComputationSession,
        localClassesNavigationInfo.parentForClass,
        FirCompositeScope(scopesForLocalClass)
    )

    return this.transform(transformer, null)
}

open class FirStatusResolveTransformer(
    session: FirSession,
    scopeSession: ScopeSession,
    statusComputationSession: StatusComputationSession,
    designationMapForLocalClasses: Map<FirClassLikeDeclaration, FirClassLikeDeclaration?> = mapOf(),
    scopeForLocalClass: FirScope? = null,
) : AbstractFirStatusResolveTransformer(
    session,
    scopeSession,
    statusComputationSession,
    designationMapForLocalClasses,
    scopeForLocalClass
) {
    override fun FirDeclaration.needResolveMembers(): Boolean {
        if (this is FirRegularClass) {
            return statusComputationSession[this] != StatusComputationSession.StatusComputationStatus.Computed
        }
        return true
    }

    override fun FirDeclaration.needResolveNestedClassifiers(): Boolean {
        return true
    }

    override fun transformClassContent(
        firClass: FirClass,
        data: FirResolvedDeclarationStatus?
    ): FirStatement {
        konst computationStatus = statusComputationSession.startComputing(firClass)
        forceResolveStatusesOfSupertypes(firClass)
        /*
         * Status of class may be already calculated if that class was in supertypes of one of the previous classes
         */
        if (computationStatus != StatusComputationSession.StatusComputationStatus.Computed) {
            transformClassStatus(firClass)
            transformValueClassRepresentation(firClass)
        }

        return transformClass(firClass, data).also {
            statusComputationSession.endComputing(firClass)
        }
    }
}

open class FirDesignatedStatusResolveTransformer(
    session: FirSession,
    scopeSession: ScopeSession,
    private konst designation: DesignationState,
    statusComputationSession: StatusComputationSession,
    designationMapForLocalClasses: Map<FirClassLikeDeclaration, FirClassLikeDeclaration?>,
    scopeForLocalClass: FirScope?,
) : AbstractFirStatusResolveTransformer(
    session,
    scopeSession,
    statusComputationSession,
    designationMapForLocalClasses,
    scopeForLocalClass
) {
    override fun FirDeclaration.needResolveMembers(): Boolean {
        return designation.classLocated
    }

    override fun FirDeclaration.needResolveNestedClassifiers(): Boolean {
        return !designation.classLocated
    }

    override fun transformClassContent(
        firClass: FirClass,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, firClass) {
        if (designation.shouldSkipClass(firClass)) return firClass
        firClass.symbol.lazyResolveToPhase(FirResolvePhase.TYPES)
        konst classLocated = designation.classLocated
        /*
         * In designated status resolve we should resolve status only of target class and it's members
         */
        if (classLocated) {
            assert(firClass == designation.targetClass)
            konst computationStatus = statusComputationSession.startComputing(firClass)
            forceResolveStatusesOfSupertypes(firClass)
            if (computationStatus != StatusComputationSession.StatusComputationStatus.Computed) {
                firClass.transformStatus(this, statusResolver.resolveStatus(firClass, containingClass, isLocal = false))
            }
        } else {
            if (firClass.status !is FirResolvedDeclarationStatus) {
                firClass.transformStatus(this, statusResolver.resolveStatus(firClass, containingClass, isLocal = false))
                statusComputationSession.computeOnlyClassStatus(firClass)
            }
        }
        return transformClass(firClass, data).also {
            if (classLocated) statusComputationSession.endComputing(firClass)
        }
    }
}

class StatusComputationSession {
    private konst statusMap = mutableMapOf<FirClass, StatusComputationStatus>()
        .withDefault { StatusComputationStatus.NotComputed }

    operator fun get(klass: FirClass): StatusComputationStatus = statusMap.getValue(klass)

    fun startComputing(klass: FirClass): StatusComputationStatus {
        return statusMap.getOrPut(klass) { StatusComputationStatus.Computing }
    }

    fun endComputing(klass: FirClass) {
        statusMap[klass] = StatusComputationStatus.Computed
    }

    fun computeOnlyClassStatus(klass: FirClass) {
        konst existedStatus = statusMap.getValue(klass)
        if (existedStatus < StatusComputationStatus.ComputedOnlyClassStatus) {
            statusMap[klass] = StatusComputationStatus.ComputedOnlyClassStatus
        }
    }

    enum class StatusComputationStatus(konst requiresComputation: Boolean) {
        NotComputed(true),
        Computing(false),
        ComputedOnlyClassStatus(true),
        Computed(false)
    }
}

abstract class AbstractFirStatusResolveTransformer(
    final override konst session: FirSession,
    konst scopeSession: ScopeSession,
    konst statusComputationSession: StatusComputationSession,
    private konst designationMapForLocalClasses: Map<FirClassLikeDeclaration, FirClassLikeDeclaration?>,
    private konst scopeForLocalClass: FirScope?
) : FirAbstractTreeTransformer<FirResolvedDeclarationStatus?>(phase = FirResolvePhase.STATUS) {
    private konst isTransformerForLocalDeclarations: Boolean get() = scopeForLocalClass != null

    @PrivateForInline
    konst classes = mutableListOf<FirClass>()
    konst statusResolver = FirStatusResolver(session, scopeSession)

    @OptIn(PrivateForInline::class)
    konst containingClass: FirClass? get() = classes.lastOrNull()

    protected abstract fun FirDeclaration.needResolveMembers(): Boolean
    protected abstract fun FirDeclaration.needResolveNestedClassifiers(): Boolean

    override fun transformFile(file: FirFile, data: FirResolvedDeclarationStatus?): FirFile {
        withFileAnalysisExceptionWrapping(file) {
            transformDeclarationContent(file, data)
        }
        return file
    }

    override fun transformDeclarationStatus(
        declarationStatus: FirDeclarationStatus,
        data: FirResolvedDeclarationStatus?
    ): FirDeclarationStatus {
        return (data ?: declarationStatus)
    }

    @OptIn(PrivateForInline::class)
    inline fun storeClass(
        klass: FirClass,
        computeResult: () -> FirDeclaration
    ): FirDeclaration {
        classes += klass
        konst result = computeResult()
        classes.removeAt(classes.lastIndex)
        return result
    }

    override fun transformDeclaration(
        declaration: FirDeclaration,
        data: FirResolvedDeclarationStatus?
    ): FirDeclaration = whileAnalysing(session, declaration) {
        return when (declaration) {
            is FirCallableDeclaration -> {
                if (declaration is FirFunction) {
                    for (konstueParameter in declaration.konstueParameters) {
                        transformValueParameter(konstueParameter, data)
                    }
                }
                declaration
            }
            else -> {
                transformElement(declaration, data)
            }
        }
    }

    override fun transformTypeAlias(
        typeAlias: FirTypeAlias,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, typeAlias) {
        typeAlias.typeParameters.forEach { transformDeclaration(it, data) }
        typeAlias.transformStatus(this, statusResolver.resolveStatus(typeAlias, containingClass, isLocal = false))
        return transformDeclaration(typeAlias, data) as FirTypeAlias
    }

    override fun transformRegularClass(
        regularClass: FirRegularClass,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, regularClass) {
        transformClassContent(regularClass, data)
    }

    abstract fun transformClassContent(
        firClass: FirClass,
        data: FirResolvedDeclarationStatus?
    ): FirStatement

    override fun transformAnonymousObject(
        anonymousObject: FirAnonymousObject,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, anonymousObject) {
        transformClassContent(anonymousObject, data)
    }

    open fun transformDeclarationContent(
        declaration: FirDeclaration,
        data: FirResolvedDeclarationStatus?
    ): FirDeclaration {

        konst declarations = when (declaration) {
            is FirRegularClass -> declaration.declarations
            is FirAnonymousObject -> declaration.declarations
            is FirFile -> declaration.declarations
            else -> error("Not supported declaration ${declaration::class.simpleName}")
        }

        if (declaration.needResolveMembers()) {
            declarations.forEach {
                if (it !is FirClassLikeDeclaration) {
                    it.transformSingle(this, data)
                }
            }
        }
        if (declaration.needResolveNestedClassifiers()) {
            declarations.forEach {
                if (it is FirClassLikeDeclaration) {
                    it.transformSingle(this, data)
                }
            }
        }
        return declaration
    }

    override fun transformClass(
        klass: FirClass,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, klass) {
        return storeClass(klass) {
            klass.typeParameters.forEach { it.transformSingle(this, data) }
            transformDeclarationContent(klass, data)
        } as FirStatement
    }

    fun transformValueClassRepresentation(firClass: FirClass) {
        if (firClass is FirRegularClass && firClass.isInline) {
            firClass.konstueClassRepresentation = computeValueClassRepresentation(firClass, session)
        }
    }

    fun transformClassStatus(firClass: FirClass) {
        firClass.transformStatus(this, statusResolver.resolveStatus(firClass, containingClass, isLocal = false))
    }

    fun forceResolveStatusesOfSupertypes(regularClass: FirClass) {
        for (superTypeRef in regularClass.superTypeRefs) {
            forceResolveStatusOfCorrespondingClass(superTypeRef)
        }
    }

    private fun forceResolveStatusOfCorrespondingClass(typeRef: FirTypeRef) {
        konst superClassSymbol = typeRef.coneType.toSymbol(session)
        if (isTransformerForLocalDeclarations) {
            if (superClassSymbol is FirClassSymbol) {
                superClassSymbol.lazyResolveToPhaseWithCallableMembers(FirResolvePhase.STATUS)
            } else {
                superClassSymbol?.lazyResolveToPhase(FirResolvePhase.STATUS)
            }
        } else {
            superClassSymbol?.lazyResolveToPhase(FirResolvePhase.STATUS.previous)
        }

        when (superClassSymbol) {
            is FirRegularClassSymbol -> forceResolveStatusesOfClass(superClassSymbol.fir)
            is FirTypeAliasSymbol -> forceResolveStatusOfCorrespondingClass(superClassSymbol.fir.expandedTypeRef)
            is FirTypeParameterSymbol, is FirAnonymousObjectSymbol, null -> {}
        }
    }

    private fun forceResolveStatusesOfClass(regularClass: FirRegularClass) {
        if (regularClass.origin is FirDeclarationOrigin.Java || regularClass.origin == FirDeclarationOrigin.Precompiled) {
            /*
             * If regular class has no corresponding file then it is platform class,
             *   so we need to resolve supertypes of this class because they could
             *   come from kotlin sources
             */
            konst statusComputationStatus = statusComputationSession[regularClass]
            if (!statusComputationStatus.requiresComputation) return

            statusComputationSession.startComputing(regularClass)
            forceResolveStatusesOfSupertypes(regularClass)
            statusComputationSession.endComputing(regularClass)

            return
        }

        if (regularClass.origin != FirDeclarationOrigin.Source) return
        konst statusComputationStatus = statusComputationSession[regularClass]
        if (!statusComputationStatus.requiresComputation) return
        if (!resolveClassForSuperType(regularClass)) return
        statusComputationSession.endComputing(regularClass)
    }

    protected open fun resolveClassForSuperType(regularClass: FirRegularClass): Boolean {
        konst designation = DesignationState.create(regularClass.symbol, designationMapForLocalClasses, includeFile = false) ?: return false

        konst transformer = FirDesignatedStatusResolveTransformer(
            session,
            scopeSession,
            designation,
            statusComputationSession,
            designationMapForLocalClasses,
            scopeForLocalClass
        )

        designation.firstDeclaration.transformSingle(transformer, null)
        return true
    }

    private fun transformPropertyAccessor(
        propertyAccessor: FirPropertyAccessor,
        containingProperty: FirProperty,
        overriddenStatuses: List<FirResolvedDeclarationStatus> = emptyList(),
    ): Unit = whileAnalysing(session, propertyAccessor) {
        propertyAccessor.transformStatus(
            this,
            statusResolver.resolveStatus(
                propertyAccessor,
                containingClass,
                containingProperty,
                isLocal = false,
                overriddenStatuses,
            )
        )

        propertyAccessor.transformValueParameters(this, null)
    }

    override fun transformConstructor(
        constructor: FirConstructor,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, constructor) {
        constructor.transformStatus(this, statusResolver.resolveStatus(constructor, containingClass, isLocal = false))
        return transformDeclaration(constructor, data) as FirStatement
    }

    override fun transformSimpleFunction(
        simpleFunction: FirSimpleFunction,
        data: FirResolvedDeclarationStatus?,
    ): FirStatement = whileAnalysing(session, simpleFunction) {
        konst overriddenFunctions = statusResolver.getOverriddenFunctions(simpleFunction, containingClass)
        transformSimpleFunction(simpleFunction, overriddenFunctions, data)
        return simpleFunction
    }

    fun transformSimpleFunction(
        simpleFunction: FirSimpleFunction,
        overriddenFunctions: List<FirSimpleFunction>,
        data: FirResolvedDeclarationStatus? = null,
    ) {
        konst resolvedStatus = statusResolver.resolveStatus(
            simpleFunction,
            containingClass,
            isLocal = false,
            overriddenFunctions.map { it.status as FirResolvedDeclarationStatus },
        )

        simpleFunction.transformStatus(this, resolvedStatus)
        transformDeclaration(simpleFunction, data) as FirStatement
    }

    override fun transformProperty(
        property: FirProperty,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, property) {
        konst overridden = statusResolver.getOverriddenProperties(property, containingClass)
        transformProperty(property, overridden)
        return property
    }

    fun transformProperty(property: FirProperty, overriddenProperties: List<FirProperty>) {
        konst overriddenStatuses = overriddenProperties.map { it.status as FirResolvedDeclarationStatus }
        konst overriddenSetters = overriddenProperties.mapNotNull {
            konst setter = it.setter ?: return@mapNotNull null
            setter.status as FirResolvedDeclarationStatus
        }

        property.transformStatus(
            this,
            statusResolver.resolveStatus(property, containingClass, false, overriddenStatuses)
        )

        property.getter?.let { transformPropertyAccessor(it, property) }
        property.setter?.let { transformPropertyAccessor(it, property, overriddenSetters) }

        property.backingField?.let {
            it.transformStatus(
                this,
                statusResolver.resolveStatus(it, containingClass, property, isLocal = false)
            )
        }

        property.componentFunctionSymbol?.let { componentFunction ->
            if (componentFunction.fir.status.visibility == Visibilities.Unknown) {
                componentFunction.fir.replaceStatus(componentFunction.fir.status.copy(visibility = property.visibility))
            }
        }
    }

    override fun transformField(
        field: FirField,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, field) {
        field.transformStatus(this, statusResolver.resolveStatus(field, containingClass, isLocal = false))
        return transformDeclaration(field, data) as FirField
    }

    override fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, data: FirResolvedDeclarationStatus?): FirStatement {
        return propertyAccessor.also { transformProperty(it.propertySymbol.fir, data) }
    }

    override fun transformEnumEntry(
        enumEntry: FirEnumEntry,
        data: FirResolvedDeclarationStatus?
    ): FirStatement = whileAnalysing(session, enumEntry) {
        enumEntry.transformStatus(this, statusResolver.resolveStatus(enumEntry, containingClass, isLocal = false))
        return transformDeclaration(enumEntry, data) as FirEnumEntry
    }

    override fun transformValueParameter(
        konstueParameter: FirValueParameter,
        data: FirResolvedDeclarationStatus?
    ): FirStatement {
        return transformDeclaration(konstueParameter, data) as FirStatement
    }

    override fun transformTypeParameter(
        typeParameter: FirTypeParameter,
        data: FirResolvedDeclarationStatus?
    ): FirTypeParameterRef {
        return transformDeclaration(typeParameter, data) as FirTypeParameter
    }

    override fun transformBlock(block: FirBlock, data: FirResolvedDeclarationStatus?): FirStatement {
        return block
    }
}
