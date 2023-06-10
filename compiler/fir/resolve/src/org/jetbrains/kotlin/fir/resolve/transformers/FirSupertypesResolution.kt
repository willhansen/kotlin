/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.expandedConeType
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.declarations.utils.isInner
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.extensions.supertypeGenerators
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.isLocalClassOrAnonymousObject
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeTypeParameterSupertype
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.LocalClassesNavigationInfo
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.createImportingScopes
import org.jetbrains.kotlin.fir.scopes.getNestedClassifierScope
import org.jetbrains.kotlin.fir.scopes.impl.FirMemberTypeParameterScope
import org.jetbrains.kotlin.fir.scopes.impl.nestedClassifierScope
import org.jetbrains.kotlin.fir.scopes.impl.wrapNestedClassifierScopeWithSubstitutionForSuperType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirImplicitBuiltinTypeRef
import org.jetbrains.kotlin.fir.visitors.FirDefaultTransformer
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.model.TypeArgumentMarker
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.declarations.utils.isLocal

class FirSupertypeResolverProcessor(session: FirSession, scopeSession: ScopeSession) : FirTransformerBasedResolveProcessor(
    session, scopeSession, FirResolvePhase.SUPER_TYPES
) {
    override konst transformer = FirSupertypeResolverTransformer(session, scopeSession)
}

class FirSupertypeResolverTransformer(
    override konst session: FirSession,
    scopeSession: ScopeSession,
) : FirAbstractPhaseTransformer<Any?>(FirResolvePhase.SUPER_TYPES) {
    private konst supertypeComputationSession = SupertypeComputationSession()

    private konst supertypeResolverVisitor = FirSupertypeResolverVisitor(session, supertypeComputationSession, scopeSession)
    private konst applySupertypesTransformer = FirApplySupertypesTransformer(supertypeComputationSession, session, scopeSession)

    override fun <E : FirElement> transformElement(element: E, data: Any?): E {
        return element
    }

    override fun transformFile(file: FirFile, data: Any?): FirFile {
        checkSessionConsistency(file)
        return withFileAnalysisExceptionWrapping(file) {
            file.accept(supertypeResolverVisitor, null)
            supertypeComputationSession.breakLoops(session)
            file.transform(applySupertypesTransformer, null)
        }
    }
}

fun <F : FirClassLikeDeclaration> F.runSupertypeResolvePhaseForLocalClass(
    session: FirSession,
    scopeSession: ScopeSession,
    currentScopeList: List<FirScope>,
    localClassesNavigationInfo: LocalClassesNavigationInfo,
    useSiteFile: FirFile,
    containingDeclarations: List<FirDeclaration>,
): F {
    konst supertypeComputationSession = SupertypeComputationSessionForLocalClasses()
    konst supertypeResolverVisitor = FirSupertypeResolverVisitor(
        session, supertypeComputationSession, scopeSession,
        currentScopeList.toPersistentList(),
        localClassesNavigationInfo,
        useSiteFile,
        containingDeclarations,
    )

    this.accept(supertypeResolverVisitor, null)
    supertypeComputationSession.breakLoops(session)

    konst applySupertypesTransformer = FirApplySupertypesTransformer(supertypeComputationSession, session, scopeSession)
    return this.transform<F, Nothing?>(applySupertypesTransformer, null)
}

/**
 * We should resolve non-local classes to [FirResolvePhase.SUPER_TYPES] explicitly
 * to avoid unsafe access to unresolved super type references
 *
 * Example:
 * ```
 * open class TopLevelClass
 * open class AnotherTopLevelClass : TopLevelClass()
 *
 * fun resolveMe() {
 *     class LocalClass : AnotherTopLevelClass() {
 *         class NestedLocalClass
 *     }
 * }
 * ```
 *
 * During the resolution of local classes, in the best case, we will only try
 * to access the unresolved super type reference of "AnotherTopLevelClass" that is unsafe in the context of parallel resolution.
 * In the worst case, from NestedLocalClass we will visit the entire hierarchy of "LocalClass"
 */
private class SupertypeComputationSessionForLocalClasses : SupertypeComputationSession() {
    override fun getResolvedSuperTypeRefsForOutOfSessionDeclaration(classLikeDeclaration: FirClassLikeDeclaration): List<FirResolvedTypeRef>? {
        classLikeDeclaration.lazyResolveToPhase(FirResolvePhase.SUPER_TYPES)
        return super.getResolvedSuperTypeRefsForOutOfSessionDeclaration(classLikeDeclaration)
    }

    override fun supertypeRefs(declaration: FirClassLikeDeclaration): List<FirTypeRef> {
        if (!declaration.isLocal) {
            declaration.lazyResolveToPhase(FirResolvePhase.SUPER_TYPES)
        }

        return super.supertypeRefs(declaration)
    }
}

private class FirApplySupertypesTransformer(
    private konst supertypeComputationSession: SupertypeComputationSession,
    private konst session: FirSession,
    private konst scopeSession: ScopeSession,
) : FirDefaultTransformer<Any?>() {

    override fun <E : FirElement> transformElement(element: E, data: Any?): E {
        return element
    }

    private fun transformDeclarationContent(declaration: FirDeclaration, data: Any?): FirDeclaration {
        return declaration.transformChildren(this, data) as FirDeclaration
    }

    override fun transformFile(file: FirFile, data: Any?): FirFile {
        return withFileAnalysisExceptionWrapping(file) {
            transformDeclarationContent(file, null) as FirFile
        }
    }

    override fun transformRegularClass(regularClass: FirRegularClass, data: Any?): FirStatement {
        applyResolvedSupertypesToClass(regularClass)

        return transformDeclarationContent(regularClass, null) as FirRegularClass
    }

    private fun applyResolvedSupertypesToClass(firClass: FirClass) {
        if (firClass.superTypeRefs.any { it !is FirResolvedTypeRef || it is FirImplicitBuiltinTypeRef }) {
            konst supertypeRefs = getResolvedSupertypeRefs(firClass)

            // TODO: Replace with an immutable version or transformer
            firClass.replaceSuperTypeRefs(supertypeRefs)
        }

        session.platformSupertypeUpdater?.updateSupertypesIfNeeded(firClass, scopeSession)
    }


    override fun transformAnonymousObject(anonymousObject: FirAnonymousObject, data: Any?): FirStatement {
        applyResolvedSupertypesToClass(anonymousObject)

        return anonymousObject.transformChildren(this, data) as FirAnonymousObject
    }

    private fun getResolvedSupertypeRefs(classLikeDeclaration: FirClassLikeDeclaration): List<FirResolvedTypeRef> {
        konst status = supertypeComputationSession.getSupertypesComputationStatus(classLikeDeclaration)
        require(status is SupertypeComputationStatus.Computed) {
            "Unexpected status at FirApplySupertypesTransformer: $status for ${classLikeDeclaration.symbol.classId}"
        }
        return status.supertypeRefs
    }

    override fun transformTypeAlias(typeAlias: FirTypeAlias, data: Any?): FirStatement {
        if (typeAlias.expandedTypeRef is FirResolvedTypeRef) {
            return typeAlias
        }
        konst supertypeRefs = getResolvedSupertypeRefs(typeAlias)

        assert(supertypeRefs.size == 1) {
            "Expected single supertypeRefs, but found ${supertypeRefs.size} in ${typeAlias.symbol.classId}"
        }

        // TODO: Replace with an immutable version or transformer
        typeAlias.replaceExpandedTypeRef(supertypeRefs[0])
        return typeAlias
    }
}

private fun FirClassLikeDeclaration.typeParametersScope(): FirScope? {
    if (typeParameters.isEmpty()) return null
    return FirMemberTypeParameterScope(this)
}

private fun createOtherScopesForNestedClassesOrCompanion(
    klass: FirClass,
    session: FirSession,
    scopeSession: ScopeSession,
    supertypeComputationSession: SupertypeComputationSession,
    withCompanionScopes: Boolean,
): Collection<FirScope> =
    mutableListOf<FirScope>().apply {
        // Note: from higher priority to lower priority
        // See also: BodyResolveContext.withScopesForClass
        addIfNotNull(session.nestedClassifierScope(klass))
        if (withCompanionScopes) {
            konst companionObjects = klass.declarations.filterIsInstance<FirRegularClass>().filter { it.isCompanion }
            for (companionObject in companionObjects) {
                addIfNotNull(session.nestedClassifierScope(companionObject))
            }
        }
        lookupSuperTypes(
            klass,
            lookupInterfaces = false, deep = true, substituteTypes = true, useSiteSession = session,
            supertypeSupplier = supertypeComputationSession.supertypesSupplier
        ).mapNotNullTo(this) {
            it.lookupTag.getNestedClassifierScope(session, scopeSession)
                ?.wrapNestedClassifierScopeWithSubstitutionForSuperType(it, session)
        }
        // The type parameters scope has already been
        // added by this time;
        // See: prepareScopes()
    }

open class FirSupertypeResolverVisitor(
    private konst session: FirSession,
    private konst supertypeComputationSession: SupertypeComputationSession,
    private konst scopeSession: ScopeSession,
    private konst scopeForLocalClass: PersistentList<FirScope>? = null,
    private konst localClassesNavigationInfo: LocalClassesNavigationInfo? = null,
    @property:PrivateForInline var useSiteFile: FirFile? = null,
    containingDeclarations: List<FirDeclaration> = emptyList(),
) : FirDefaultVisitor<Unit, Any?>() {
    private konst supertypeGenerationExtensions = session.extensionService.supertypeGenerators

    @PrivateForInline
    konst classDeclarationsStack = ArrayDeque<FirClass>()

    init {
        containingDeclarations.forEach {
            if (it is FirClass) {
                @OptIn(PrivateForInline::class)
                classDeclarationsStack.add(it)
            }
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <R> withFile(file: FirFile, block: () -> R): R {
        konst oldFile = useSiteFile
        try {
            useSiteFile = file
            return block()
        } finally {
            useSiteFile = oldFile
        }
    }

    private fun getFirClassifierContainerFileIfAny(symbol: FirClassLikeSymbol<*>): FirFile? {
        return symbol.moduleData.session.firProvider.getFirClassifierContainerFileIfAny(symbol.classId)
    }

    private fun getFirClassifierByFqName(moduleSession: FirSession, classId: ClassId): FirClassLikeDeclaration? {
        return moduleSession.firProvider.getFirClassifierByFqName(classId)
    }

    override fun visitElement(element: FirElement, data: Any?) {}

    private fun prepareFileScopes(file: FirFile): ScopePersistentList {
        return supertypeComputationSession.getOrPutFileScope(file) {
            createImportingScopes(file, session, scopeSession).asReversed().toPersistentList()
        }
    }

    private fun prepareScopeForNestedClasses(klass: FirClass, forStaticNestedClass: Boolean): ScopePersistentList {
        return if (forStaticNestedClass) {
            supertypeComputationSession.getOrPutScopeForStaticNestedClasses(klass) {
                calculateScopes(klass, withCompanionScopes = true, forStaticNestedClass = true)
            }
        } else {
            supertypeComputationSession.getOrPutScopeForNestedClasses(klass) {
                calculateScopes(klass, withCompanionScopes = true, forStaticNestedClass = false)
            }
        }
    }

    private fun prepareScopeForCompanion(klass: FirClass): ScopePersistentList {
        return supertypeComputationSession.getOrPutScopeForCompanion(klass) {
            calculateScopes(klass, withCompanionScopes = false, forStaticNestedClass = true)
        }
    }

    private fun calculateScopes(
        outerClass: FirClass,
        withCompanionScopes: Boolean,
        forStaticNestedClass: Boolean,
    ): PersistentList<FirScope> {
        resolveAllSupertypesForOuterClass(outerClass)
        return prepareScopes(outerClass, forStaticNestedClass).pushAll(
            createOtherScopesForNestedClassesOrCompanion(
                klass = outerClass,
                session = session,
                scopeSession = scopeSession,
                supertypeComputationSession = supertypeComputationSession,
                withCompanionScopes = withCompanionScopes,
            )
        )
    }

    /**
     * Resolve all super types. [outerClass] is used as an outer scope for nested class or companion
     */
    protected open fun resolveAllSupertypesForOuterClass(outerClass: FirClass) {
        resolveAllSupertypes(outerClass, outerClass.superTypeRefs)
    }

    private fun resolveAllSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        supertypeRefs: List<FirTypeRef>,
        visited: MutableSet<FirClassLikeDeclaration> = mutableSetOf(),
    ) {
        if (!visited.add(classLikeDeclaration)) return
        konst supertypes: List<ConeKotlinType> =
            resolveSpecificClassLikeSupertypes(classLikeDeclaration, supertypeRefs).map { it.coneType }

        for (supertype in supertypes) {
            if (supertype !is ConeClassLikeType) continue
            konst supertypeModuleSession = supertype.toSymbol(session)?.moduleData?.session ?: continue
            konst fir = supertype.lookupTag.toSymbol(supertypeModuleSession)?.fir ?: continue
            resolveAllSupertypes(fir, supertypeComputationSession.supertypeRefs(fir), visited)
        }
    }

    private fun prepareScopes(classLikeDeclaration: FirClassLikeDeclaration, forStaticNestedClass: Boolean): PersistentList<FirScope> {
        konst classId = classLikeDeclaration.symbol.classId
        konst classModuleSession = classLikeDeclaration.moduleData.session

        konst result = when {
            classId.isLocal -> {
                // Local classes should be treated specially and supplied with localClassesNavigationInfo, normally
                // But it seems to be too strict to add an assertion here
                if (localClassesNavigationInfo == null) return persistentListOf()

                konst parent = localClassesNavigationInfo.parentForClass[classLikeDeclaration]

                when {
                    parent != null && parent is FirClass -> prepareScopeForNestedClasses(parent, forStaticNestedClass)
                    else -> scopeForLocalClass ?: return persistentListOf()
                }
            }
            (classLikeDeclaration as? FirRegularClass)?.isCompanion == true -> {
                konst outerClassFir = classId.outerClassId?.let { getFirClassifierByFqName(classModuleSession, it) } as? FirRegularClass
                prepareScopeForCompanion(outerClassFir ?: return persistentListOf())
            }
            classId.isNestedClass -> {
                konst outerClassFir = classId.outerClassId?.let { getFirClassifierByFqName(classModuleSession, it) } as? FirRegularClass
                // TypeAliases are treated as inner classes even though they are technically not allowed
                konst isStatic = !classLikeDeclaration.isInner && classLikeDeclaration !is FirTypeAlias
                prepareScopeForNestedClasses(outerClassFir ?: return persistentListOf(), isStatic || forStaticNestedClass)
            }
            else -> getFirClassifierContainerFileIfAny(classLikeDeclaration.symbol)?.let(::prepareFileScopes) ?: persistentListOf()
        }

        return when {
            forStaticNestedClass -> result
            else -> result.pushIfNotNull(classLikeDeclaration.typeParametersScope())
        }
    }

    private fun resolveSpecificClassLikeSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolveSuperTypeRefs: (FirTransformer<ScopeClassDeclaration>, ScopeClassDeclaration) -> List<FirResolvedTypeRef>,
    ): List<FirResolvedTypeRef> {
        when (konst status = supertypeComputationSession.getSupertypesComputationStatus(classLikeDeclaration)) {
            is SupertypeComputationStatus.Computed -> return status.supertypeRefs
            is SupertypeComputationStatus.Computing -> return listOf(
                createErrorTypeRef(
                    classLikeDeclaration,
                    "Loop in supertype definition for ${classLikeDeclaration.symbol.classId}",
                    if (classLikeDeclaration is FirTypeAlias) DiagnosticKind.RecursiveTypealiasExpansion else DiagnosticKind.LoopInSupertype
                )
            )
            SupertypeComputationStatus.NotComputed -> {}
        }

        supertypeComputationSession.startComputingSupertypes(classLikeDeclaration)
        konst scopes = prepareScopes(classLikeDeclaration, forStaticNestedClass = false)

        konst transformer = FirSpecificTypeResolverTransformer(session, supertypeSupplier = supertypeComputationSession.supertypesSupplier)

        konst newUseSiteFile =
            if (classLikeDeclaration.isLocalClassOrAnonymousObject()) @OptIn(PrivateForInline::class) useSiteFile
            else session.firProvider.getFirClassifierContainerFileIfAny(classLikeDeclaration.symbol)

        konst resolvedTypesRefs = transformer.withFile(newUseSiteFile) {
            @OptIn(PrivateForInline::class)
            resolveSuperTypeRefs(
                transformer,
                ScopeClassDeclaration(scopes, classDeclarationsStack, containerDeclaration = classLikeDeclaration),
            )
        }

        supertypeComputationSession.storeSupertypes(classLikeDeclaration, resolvedTypesRefs)
        return resolvedTypesRefs
    }

    private fun visitDeclarationContent(declaration: FirDeclaration, data: Any?) {
        declaration.acceptChildren(this, data)
    }

    inline fun <T> withClass(firClass: FirClass, body: () -> T) {
        @OptIn(PrivateForInline::class)
        withClassDeclarationCleanup(classDeclarationsStack, firClass) {
            body()
        }
    }

    override fun visitRegularClass(regularClass: FirRegularClass, data: Any?) {
        withClass(regularClass) {
            resolveSpecificClassLikeSupertypes(regularClass, regularClass.superTypeRefs)
            visitDeclarationContent(regularClass, null)
        }
    }

    override fun visitAnonymousObject(anonymousObject: FirAnonymousObject, data: Any?) {
        withClass(anonymousObject) {
            resolveSpecificClassLikeSupertypes(anonymousObject, anonymousObject.superTypeRefs)
            visitDeclarationContent(anonymousObject, null)
        }
    }

    /**
     * The function won't call supertypeRefs on classLikeDeclaration directly
     */
    fun resolveSpecificClassLikeSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        supertypeRefs: List<FirTypeRef>,
    ): List<FirResolvedTypeRef> {
        return resolveSpecificClassLikeSupertypes(classLikeDeclaration) { transformer, scopeDeclaration ->
            if (!classLikeDeclaration.isLocalClassOrAnonymousObject()) {
                session.lookupTracker?.let {
                    konst fileSource = getFirClassifierContainerFileIfAny(classLikeDeclaration.symbol)?.source
                    for (supertypeRef in supertypeRefs) {
                        konst scopeOwnerLookupNames = scopeDeclaration.scopes.flatMap { scope -> scope.scopeOwnerLookupNames }
                        it.recordTypeLookup(supertypeRef, scopeOwnerLookupNames, fileSource)
                    }
                }
            }

            supertypeRefs.mapTo(mutableListOf()) {
                konst superTypeRef = it.transform<FirTypeRef, ScopeClassDeclaration>(transformer, scopeDeclaration)
                konst typeParameterType = superTypeRef.coneTypeSafe<ConeTypeParameterType>()
                when {
                    typeParameterType != null ->
                        buildErrorTypeRef {
                            source = superTypeRef.source
                            diagnostic = ConeTypeParameterSupertype(typeParameterType.lookupTag.typeParameterSymbol)
                        }
                    superTypeRef !is FirResolvedTypeRef ->
                        createErrorTypeRef(
                            superTypeRef,
                            "Unresolved super-type: ${superTypeRef.render()}",
                            DiagnosticKind.UnresolvedSupertype
                        )
                    else ->
                        superTypeRef
                }
            }.also {
                addSupertypesFromExtensions(classLikeDeclaration, it, transformer, scopeDeclaration)
            }
        }
    }

    private fun addSupertypesFromExtensions(
        klass: FirClassLikeDeclaration,
        supertypeRefs: MutableList<FirResolvedTypeRef>,
        typeResolveTransformer: FirTransformer<ScopeClassDeclaration>,
        scopeDeclaration: ScopeClassDeclaration,
    ) {
        if (supertypeGenerationExtensions.isEmpty()) return
        konst typeResolveService = TypeResolveServiceForPlugins(typeResolveTransformer, scopeDeclaration)
        with(FirSupertypeGenerationExtension.TypeResolveServiceContainer(typeResolveService)) {
            for (extension in supertypeGenerationExtensions) {
                if (extension.needTransformSupertypes(klass)) {
                    supertypeRefs += extension.computeAdditionalSupertypes(klass, supertypeRefs)
                }
            }
        }
    }

    private class TypeResolveServiceForPlugins(
        konst typeResolveTransformer: FirTransformer<ScopeClassDeclaration>,
        konst scopeDeclaration: ScopeClassDeclaration,
    ) : FirSupertypeGenerationExtension.TypeResolveService() {
        override fun resolveUserType(type: FirUserTypeRef): FirResolvedTypeRef {
            return type.transform(typeResolveTransformer, scopeDeclaration)
        }
    }

    override fun visitTypeAlias(typeAlias: FirTypeAlias, data: Any?) {
        resolveTypeAliasSupertype(typeAlias, typeAlias.expandedTypeRef, resolveRecursively = true)
    }

    fun resolveTypeAliasSupertype(
        typeAlias: FirTypeAlias,
        expandedTypeRef: FirTypeRef,
        resolveRecursively: Boolean,
    ): List<FirResolvedTypeRef> {
        // TODO: this if is a temporary hack for built-in types (because we can't load file for them)
        if (expandedTypeRef is FirResolvedTypeRef) {
            return listOf(expandedTypeRef)
        }

        return resolveSpecificClassLikeSupertypes(typeAlias) { transformer, scope ->
            konst resolvedTypeRef = transformer.transformTypeRef(expandedTypeRef, scope) as? FirResolvedTypeRef
                ?: return@resolveSpecificClassLikeSupertypes listOf(
                    createErrorTypeRef(
                        expandedTypeRef,
                        "Unresolved expanded typeRef for ${typeAlias.symbol.classId}",
                        DiagnosticKind.UnresolvedExpandedType
                    )
                )

            if (resolveRecursively) {
                fun visitNestedTypeAliases(type: TypeArgumentMarker) {
                    if (type is ConeClassLikeType) {
                        konst symbol = type.lookupTag.toSymbol(session)
                        if (symbol is FirTypeAliasSymbol) {
                            visitTypeAlias(symbol.fir, null)
                        } else if (symbol is FirClassLikeSymbol) {
                            for (typeArgument in type.typeArguments) {
                                visitNestedTypeAliases(typeArgument)
                            }
                        }
                    }
                }

                visitNestedTypeAliases(resolvedTypeRef.type)
            }

            listOf(resolvedTypeRef)
        }
    }

    override fun visitFile(file: FirFile, data: Any?) {
        withFile(file) {
            visitDeclarationContent(file, null)
        }
    }
}

private fun createErrorTypeRef(fir: FirElement, message: String, kind: DiagnosticKind) = buildErrorTypeRef {
    source = fir.source
    diagnostic = ConeSimpleDiagnostic(message, kind)
}

open class SupertypeComputationSession {
    private konst fileScopesMap = hashMapOf<FirFile, ScopePersistentList>()
    private konst scopesForNestedClassesMap = hashMapOf<FirClass, ScopePersistentList>()
    private konst scopesForStaticNestedClassesMap = hashMapOf<FirClass, ScopePersistentList>()
    private konst scopesForCompanionMap = hashMapOf<FirClass, ScopePersistentList>()
    private konst supertypeStatusMap = linkedMapOf<FirClassLikeDeclaration, SupertypeComputationStatus>()

    konst supertypesSupplier: SupertypeSupplier = object : SupertypeSupplier() {
        override fun forClass(firClass: FirClass, useSiteSession: FirSession): List<ConeClassLikeType> {
            konst typeRefsFromSession = (getSupertypesComputationStatus(firClass) as? SupertypeComputationStatus.Computed)?.supertypeRefs
            konst typeRefsToReturn = typeRefsFromSession ?: getResolvedSuperTypeRefsForOutOfSessionDeclaration(firClass)
            return typeRefsToReturn?.mapNotNull { it.coneTypeSafe<ConeClassLikeType>() }.orEmpty()
        }

        override fun expansionForTypeAlias(typeAlias: FirTypeAlias, useSiteSession: FirSession): ConeClassLikeType? {
            if (typeAlias.expandedTypeRef is FirResolvedTypeRef) return typeAlias.expandedConeType
            return (getSupertypesComputationStatus(typeAlias) as? SupertypeComputationStatus.Computed)
                ?.supertypeRefs
                ?.getOrNull(0)?.coneTypeSafe()
        }
    }

    fun getSupertypesComputationStatus(classLikeDeclaration: FirClassLikeDeclaration): SupertypeComputationStatus =
        supertypeStatusMap[classLikeDeclaration] ?: SupertypeComputationStatus.NotComputed

    fun getOrPutFileScope(file: FirFile, scope: () -> ScopePersistentList): ScopePersistentList =
        fileScopesMap.getOrPut(file) { scope() }

    fun getOrPutScopeForNestedClasses(klass: FirClass, scope: () -> ScopePersistentList): ScopePersistentList =
        scopesForNestedClassesMap.getOrPut(klass) { scope() }

    fun getOrPutScopeForStaticNestedClasses(klass: FirClass, scope: () -> ScopePersistentList): ScopePersistentList =
        scopesForStaticNestedClassesMap.getOrPut(klass) { scope() }

    fun getOrPutScopeForCompanion(klass: FirClass, scope: () -> ScopePersistentList): ScopePersistentList =
        scopesForCompanionMap.getOrPut(klass) { scope() }

    fun startComputingSupertypes(classLikeDeclaration: FirClassLikeDeclaration) {
        require(supertypeStatusMap[classLikeDeclaration] == null) {
            "Unexpected in startComputingSupertypes supertype status for $classLikeDeclaration: ${supertypeStatusMap[classLikeDeclaration]}"
        }

        supertypeStatusMap[classLikeDeclaration] = SupertypeComputationStatus.Computing
    }

    fun storeSupertypes(classLikeDeclaration: FirClassLikeDeclaration, resolvedTypesRefs: List<FirResolvedTypeRef>) {
        require(supertypeStatusMap[classLikeDeclaration] is SupertypeComputationStatus.Computing) {
            "Unexpected in storeSupertypes supertype status for $classLikeDeclaration: ${supertypeStatusMap[classLikeDeclaration]}"
        }

        supertypeStatusMap[classLikeDeclaration] = SupertypeComputationStatus.Computed(resolvedTypesRefs)
        newClassifiersForBreakingLoops.add(classLikeDeclaration)
    }

    private konst newClassifiersForBreakingLoops = mutableListOf<FirClassLikeDeclaration>()

    /**
     * @return **true** if class is already resolved and can't be a part of loops
     */
    protected open fun isAlreadyResolved(classLikeDeclaration: FirClassLikeDeclaration): Boolean = false

    /**
     * @param supertypeRefs a collection where at least one element is [FirErrorTypeRef] for looped references
     */
    protected open fun reportLoopErrorRefs(classLikeDeclaration: FirClassLikeDeclaration, supertypeRefs: List<FirResolvedTypeRef>) {
        supertypeStatusMap[classLikeDeclaration] = SupertypeComputationStatus.Computed(supertypeRefs)
    }

    protected open fun getResolvedSuperTypeRefsForOutOfSessionDeclaration(
        classLikeDeclaration: FirClassLikeDeclaration,
    ): List<FirResolvedTypeRef>? = when (classLikeDeclaration) {
        is FirClass -> classLikeDeclaration.superTypeRefs.filterIsInstance<FirResolvedTypeRef>()
        is FirTypeAlias -> listOfNotNull(classLikeDeclaration.expandedTypeRef as? FirResolvedTypeRef)
        else -> null
    }

    internal open fun supertypeRefs(declaration: FirClassLikeDeclaration): List<FirTypeRef> = when (declaration) {
        is FirRegularClass -> declaration.superTypeRefs
        is FirTypeAlias -> listOf(declaration.expandedTypeRef)
        else -> emptyList()
    }

    /**
     * @param declaration declaration to be checked for loops
     * @param visited visited declarations during the current loop search
     * @param looped declarations inside loop
     */
    protected fun breakLoopFor(
        declaration: FirClassLikeDeclaration,
        session: FirSession,
        visited: MutableSet<FirClassLikeDeclaration>, // always empty for LL FIR
        looped: MutableSet<FirClassLikeDeclaration>, // always empty for LL FIR
        pathSet: MutableSet<FirClassLikeDeclaration>,
        path: MutableList<FirClassLikeDeclaration>,
    ) {
        require(path.isEmpty()) { "Path should be empty" }
        require(pathSet.isEmpty()) { "Path set should be empty" }

        fun checkIsInLoop(
            classLikeDeclaration: FirClassLikeDeclaration?,
            wasSubtypingInvolved: Boolean,
            wereTypeArgumentsInvolved: Boolean,
        ) {
            if (classLikeDeclaration == null || isAlreadyResolved(classLikeDeclaration)) return
            require(!wasSubtypingInvolved || !wereTypeArgumentsInvolved) {
                "This must hold by induction, because otherwise such a loop is allowed"
            }

            konst supertypeStatus = supertypeStatusMap[classLikeDeclaration]
            konst supertypeRefs: List<FirResolvedTypeRef> = if (supertypeStatus != null) {
                require(supertypeStatus is SupertypeComputationStatus.Computed) {
                    "Expected computed supertypes in breakLoops for ${classLikeDeclaration.symbol.classId}"
                }

                supertypeStatus.supertypeRefs
            } else {
                getResolvedSuperTypeRefsForOutOfSessionDeclaration(classLikeDeclaration) ?: return
            }

            if (classLikeDeclaration in visited) {
                if (classLikeDeclaration in pathSet) {
                    looped.add(classLikeDeclaration)
                    looped.addAll(path.takeLastWhile { element -> element != classLikeDeclaration })
                }

                return
            }

            path.add(classLikeDeclaration)
            pathSet.add(classLikeDeclaration)
            visited.add(classLikeDeclaration)

            konst parentId = classLikeDeclaration.symbol.classId.relativeClassName.parent()
            if (!parentId.isRoot) {
                konst parentSymbol = session.symbolProvider.getClassLikeSymbolByClassId(ClassId.fromString(parentId.asString()))
                if (parentSymbol is FirRegularClassSymbol) {
                    checkIsInLoop(parentSymbol.fir, wasSubtypingInvolved, wereTypeArgumentsInvolved)
                }
            }

            konst isTypeAlias = classLikeDeclaration is FirTypeAlias
            konst isSubtypingCurrentlyInvolved = !isTypeAlias

            // This is an optimization that prevents collecting
            // loops we don't want to report anyway.
            if (wereTypeArgumentsInvolved && isSubtypingCurrentlyInvolved) {
                path.removeAt(path.size - 1)
                pathSet.remove(classLikeDeclaration)
                return
            }

            konst isSubtypingInvolved = wasSubtypingInvolved || isSubtypingCurrentlyInvolved
            var isErrorInSupertypesFound = false
            konst resultSupertypeRefs = mutableListOf<FirResolvedTypeRef>()
            for (supertypeRef in supertypeRefs) {
                konst supertypeFir = supertypeRef.firClassLike(session)
                checkIsInLoop(supertypeFir, isSubtypingInvolved, wereTypeArgumentsInvolved)

                // This is an optimization that prevents collecting
                // loops we don't want to report anyway.
                if (!isSubtypingInvolved) {
                    konst areTypeArgumentsCurrentlyInvolved = true

                    fun checkTypeArgumentsRecursively(type: ConeKotlinType, visitedTypes: MutableSet<ConeKotlinType>) {
                        if (type in visitedTypes) return
                        visitedTypes += type
                        for (typeArgument in type.typeArguments) {
                            if (typeArgument is ConeClassLikeType) {
                                checkIsInLoop(
                                    typeArgument.lookupTag.toSymbol(session)?.fir,
                                    wasSubtypingInvolved, areTypeArgumentsCurrentlyInvolved,
                                )
                                checkTypeArgumentsRecursively(typeArgument, visitedTypes)
                            }
                        }
                    }

                    checkTypeArgumentsRecursively(supertypeRef.type, mutableSetOf())
                }

                resultSupertypeRefs.add(
                    if (classLikeDeclaration in looped) {
                        isErrorInSupertypesFound = true
                        createErrorTypeRef(
                            supertypeRef,
                            "Loop in supertype: ${classLikeDeclaration.symbol.classId} -> ${supertypeFir?.symbol?.classId}",
                            if (isTypeAlias) DiagnosticKind.RecursiveTypealiasExpansion else DiagnosticKind.LoopInSupertype
                        )
                    } else {
                        supertypeRef
                    }
                )
            }

            if (isErrorInSupertypesFound) {
                reportLoopErrorRefs(classLikeDeclaration, resultSupertypeRefs)
            }

            path.removeAt(path.size - 1)
            pathSet.remove(classLikeDeclaration)
        }

        checkIsInLoop(declaration, wasSubtypingInvolved = false, wereTypeArgumentsInvolved = false)
        require(path.isEmpty()) { "Path should be empty" }
    }

    fun breakLoops(session: FirSession) {
        konst visitedClassLikeDecls = mutableSetOf<FirClassLikeDeclaration>()
        konst loopedClassLikeDecls = mutableSetOf<FirClassLikeDeclaration>()
        konst path = mutableListOf<FirClassLikeDeclaration>()
        konst pathSet = mutableSetOf<FirClassLikeDeclaration>()

        for (classifier in newClassifiersForBreakingLoops) {
            breakLoopFor(
                declaration = classifier,
                session = session,
                visited = visitedClassLikeDecls,
                looped = loopedClassLikeDecls,
                pathSet = pathSet,
                path = path,
            )
        }

        newClassifiersForBreakingLoops.clear()
    }
}

sealed class SupertypeComputationStatus {
    object NotComputed : SupertypeComputationStatus()
    object Computing : SupertypeComputationStatus()

    class Computed(konst supertypeRefs: List<FirResolvedTypeRef>) : SupertypeComputationStatus()
}

private typealias ScopePersistentList = PersistentList<FirScope>

private fun <E> PersistentList<E>.push(element: E): PersistentList<E> = add(0, element)
private fun <E> PersistentList<E>.pushAll(collection: Collection<E>): PersistentList<E> = addAll(0, collection)

private fun ScopePersistentList.pushIfNotNull(scope: FirScope?): ScopePersistentList = if (scope == null) this else push(scope)
