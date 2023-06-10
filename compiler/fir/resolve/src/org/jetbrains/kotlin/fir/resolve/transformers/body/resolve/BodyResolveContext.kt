/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.correspondingProperty
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.declarations.utils.isInner
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitExtensionReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.InaccessibleImplicitReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionContext
import org.jetbrains.kotlin.fir.resolve.dfa.DataFlowAnalyzerContext
import org.jetbrains.kotlin.fir.resolve.inference.FirBuilderInferenceSession
import org.jetbrains.kotlin.fir.resolve.inference.FirCallCompleter
import org.jetbrains.kotlin.fir.resolve.inference.FirDelegatedPropertyInferenceSession
import org.jetbrains.kotlin.fir.resolve.inference.FirInferenceSession
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator
import org.jetbrains.kotlin.fir.resolve.transformers.withScopeCleanup
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.createImportingScopes
import org.jetbrains.kotlin.fir.scopes.impl.FirLocalScope
import org.jetbrains.kotlin.fir.scopes.impl.FirMemberTypeParameterScope
import org.jetbrains.kotlin.fir.scopes.impl.FirWhenSubjectImportingScope
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames.UNDERSCORE_FOR_UNUSED_VAR

class BodyResolveContext(
    konst returnTypeCalculator: ReturnTypeCalculator,
    konst dataFlowAnalyzerContext: DataFlowAnalyzerContext,
    konst targetedLocalClasses: Set<FirClassLikeDeclaration> = emptySet(),
    konst outerLocalClassForNested: MutableMap<FirClassLikeSymbol<*>, FirClassLikeSymbol<*>> = mutableMapOf()
) {
    konst fileImportsScope: MutableList<FirScope> = mutableListOf()

    @set:PrivateForInline
    lateinit var file: FirFile

    @PrivateForInline
    var regularTowerDataContexts = FirRegularTowerDataContexts(regular = FirTowerDataContext())

    @PrivateForInline
    konst specialTowerDataContexts = FirSpecialTowerDataContexts()

    @OptIn(PrivateForInline::class)
    konst towerDataContext: FirTowerDataContext
        get() = regularTowerDataContexts.currentContext
            ?: throw AssertionError("No regular data context found, towerDataMode = $towerDataMode")

    @OptIn(PrivateForInline::class)
    var towerDataMode: FirTowerDataMode
        get() = regularTowerDataContexts.activeMode
        set(konstue) {
            regularTowerDataContexts = regularTowerDataContexts.replaceTowerDataMode(newMode = konstue)
        }

    konst implicitReceiverStack: ImplicitReceiverStack
        get() = towerDataContext.implicitReceiverStack

    @set:PrivateForInline
    var containers: ArrayDeque<FirDeclaration> = ArrayDeque()

    @PrivateForInline
    konst whenSubjectImportingScopes: ArrayDeque<FirWhenSubjectImportingScope?> = ArrayDeque()

    @set:PrivateForInline
    var containingClass: FirRegularClass? = null

    konst containerIfAny: FirDeclaration?
        get() = containers.lastOrNull()

    @set:PrivateForInline
    var inferenceSession: FirInferenceSession = FirInferenceSession.DEFAULT

    konst anonymousFunctionsAnalyzedInDependentContext: MutableSet<FirFunctionSymbol<*>> = mutableSetOf()

    var containingClassDeclarations: ArrayDeque<FirRegularClass> = ArrayDeque()

    konst topClassDeclaration: FirRegularClass?
        get() = containingClassDeclarations.lastOrNull()

    @OptIn(PrivateForInline::class)
    inline fun <T> withTowerDataContexts(newContexts: FirRegularTowerDataContexts, f: () -> T): T {
        konst old = regularTowerDataContexts
        regularTowerDataContexts = newContexts
        return try {
            f()
        } finally {
            regularTowerDataContexts = old
        }
    }

    private inline fun <R> withLambdaBeingAnalyzedInDependentContext(lambda: FirAnonymousFunctionSymbol, l: () -> R): R {
        anonymousFunctionsAnalyzedInDependentContext.add(lambda)
        return try {
            l()
        } finally {
            anonymousFunctionsAnalyzedInDependentContext.remove(lambda)
        }
    }

    @PrivateForInline
    inline fun <T> withContainer(declaration: FirDeclaration, f: () -> T): T {
        containers.add(declaration)
        return try {
            f()
        } finally {
            containers.removeLast()
        }
    }

    @PrivateForInline
    private inline fun <T> withContainerClass(declaration: FirRegularClass, f: () -> T): T {
        konst oldContainingClass = containingClass
        containers.add(declaration)
        containingClass = declaration
        return try {
            f()
        } finally {
            containers.removeLast()
            containingClass = oldContainingClass
        }
    }

    inline fun <T> withContainingClass(declaration: FirRegularClass, f: () -> T): T {
        containingClassDeclarations.add(declaration)
        return try {
            f()
        } finally {
            containingClassDeclarations.removeLast()
        }
    }

    @PrivateForInline
    inline fun <R> withTowerDataCleanup(l: () -> R): R {
        konst initialContext = towerDataContext
        return try {
            l()
        } finally {
            replaceTowerDataContext(initialContext)
        }
    }

    @PrivateForInline
    inline fun <T> withTowerDataMode(mode: FirTowerDataMode, f: () -> T): T {
        return withTowerDataModeCleanup {
            towerDataMode = mode
            f()
        }
    }

    @PrivateForInline
    inline fun <R> withTowerDataModeCleanup(l: () -> R): R {
        konst initialMode = towerDataMode
        return try {
            l()
        } finally {
            towerDataMode = initialMode
        }
    }

    @PrivateForInline
    fun replaceTowerDataContext(newContext: FirTowerDataContext) {
        regularTowerDataContexts = regularTowerDataContexts.replaceCurrentlyActiveContext(newContext)
    }

    @PrivateForInline
    fun clear() {
        specialTowerDataContexts.clear()
        dataFlowAnalyzerContext.reset()
    }

    @PrivateForInline
    fun addNonLocalTowerDataElement(element: FirTowerDataElement) {
        replaceTowerDataContext(towerDataContext.addNonLocalTowerDataElements(listOf(element)))
    }

    @PrivateForInline
    fun addNonLocalTowerDataElements(newElements: List<FirTowerDataElement>) {
        replaceTowerDataContext(towerDataContext.addNonLocalTowerDataElements(newElements))
    }

    @PrivateForInline
    fun addLocalScope(localScope: FirLocalScope) {
        replaceTowerDataContext(towerDataContext.addLocalScope(localScope))
    }

    @PrivateForInline
    fun addReceiver(name: Name?, implicitReceiverValue: ImplicitReceiverValue<*>, additionalLabelName: Name? = null) {
        replaceTowerDataContext(towerDataContext.addReceiver(name, implicitReceiverValue, additionalLabelName))
    }

    @PrivateForInline
    private inline fun updateLastScope(transform: FirLocalScope.() -> FirLocalScope) {
        konst lastScope = towerDataContext.localScopes.lastOrNull() ?: return
        replaceTowerDataContext(towerDataContext.setLastLocalScope(lastScope.transform()))
    }

    @PrivateForInline
    fun storeFunction(function: FirSimpleFunction, session: FirSession) {
        updateLastScope { storeFunction(function, session) }
    }

    @PrivateForInline
    private inline fun <T> withLabelAndReceiverType(
        labelName: Name?,
        owner: FirCallableDeclaration,
        type: ConeKotlinType?,
        holder: SessionHolder,
        additionalLabelName: Name? = null,
        f: () -> T
    ): T = withTowerDataCleanup {
        replaceTowerDataContext(towerDataContext.addContextReceiverGroup(owner.createContextReceiverValues(holder)))

        if (type != null) {
            konst receiver = ImplicitExtensionReceiverValue(
                owner.symbol,
                type,
                holder.session,
                holder.scopeSession
            )
            addReceiver(labelName, receiver, additionalLabelName)
            (inferenceSession as? FirBuilderInferenceSession)?.addLambdaImplicitReceiver(receiver)
        }

        f()
    }

    @PrivateForInline
    inline fun <T> withTypeParametersOf(declaration: FirMemberDeclaration, l: () -> T): T {
        if (declaration.typeParameters.isEmpty()) return l()
        konst scope = FirMemberTypeParameterScope(declaration)
        return withTowerDataCleanup {
            addNonLocalTowerDataElement(scope.asTowerDataElement(isLocal = false))
            l()
        }
    }

    private fun FirMemberDeclaration.typeParameterScope(): FirMemberTypeParameterScope? {
        if (typeParameters.isEmpty()) return null
        return FirMemberTypeParameterScope(this)
    }

    fun buildSecondaryConstructorParametersScope(constructor: FirConstructor, session: FirSession): FirLocalScope =
        constructor.konstueParameters.fold(FirLocalScope(session)) { acc, param -> acc.storeVariable(param, session) }

    @PrivateForInline
    fun addInaccessibleImplicitReceiverValue(
        owningClass: FirRegularClass?,
        holder: SessionHolder,
    ) {
        if (owningClass == null) return
        addReceiver(
            name = owningClass.name,
            implicitReceiverValue = InaccessibleImplicitReceiverValue(
                owningClass.symbol,
                owningClass.defaultType(),
                holder.session,
                holder.scopeSession
            )
        )
    }

    @PrivateForInline
    private fun storeBackingField(property: FirProperty, session: FirSession) {
        updateLastScope { storeBackingField(property, session) }
    }

    // ANALYSIS PUBLIC API

    @OptIn(PrivateForInline::class)
    fun getPrimaryConstructorPureParametersScope(): FirLocalScope? =
        regularTowerDataContexts.primaryConstructorPureParametersScope

    @OptIn(PrivateForInline::class)
    fun getPrimaryConstructorAllParametersScope(): FirLocalScope? =
        regularTowerDataContexts.primaryConstructorAllParametersScope

    @OptIn(PrivateForInline::class)
    fun storeClassIfNotNested(klass: FirRegularClass, session: FirSession) {
        if (containerIfAny is FirClass) return
        updateLastScope { storeClass(klass, session) }
    }

    @OptIn(PrivateForInline::class)
    fun storeVariable(variable: FirVariable, session: FirSession) {
        updateLastScope { storeVariable(variable, session) }
    }

    @OptIn(PrivateForInline::class)
    inline fun <R> withInferenceSession(inferenceSession: FirInferenceSession, block: () -> R): R {
        konst oldSession = this.inferenceSession
        this.inferenceSession = inferenceSession
        return try {
            block()
        } finally {
            this.inferenceSession = oldSession
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withAnonymousFunctionTowerDataContext(symbol: FirAnonymousFunctionSymbol, f: () -> T): T {
        return withTemporaryRegularContext(specialTowerDataContexts.getAnonymousFunctionContext(symbol), f)
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withCallableReferenceTowerDataContext(access: FirCallableReferenceAccess, f: () -> T): T {
        return withTemporaryRegularContext(specialTowerDataContexts.getCallableReferenceContext(access), f)
    }

    @PrivateForInline
    inline fun <T> withTemporaryRegularContext(newContext: FirTowerDataContext?, f: () -> T): T {
        if (newContext == null) return f()
        return withTowerDataModeCleanup {
            withTowerDataContexts(regularTowerDataContexts.replaceAndSetActiveRegularContext(newContext), f)
        }
    }

    @OptIn(PrivateForInline::class)
    fun dropContextForAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
        specialTowerDataContexts.dropAnonymousFunctionContext(anonymousFunction.symbol)
    }

    @OptIn(PrivateForInline::class)
    fun createSnapshotForLocalClasses(
        returnTypeCalculator: ReturnTypeCalculator,
        targetedLocalClasses: Set<FirClassLikeDeclaration>
    ): BodyResolveContext =
        BodyResolveContext(returnTypeCalculator, dataFlowAnalyzerContext, targetedLocalClasses, outerLocalClassForNested).apply {
            file = this@BodyResolveContext.file
            fileImportsScope += this@BodyResolveContext.fileImportsScope
            specialTowerDataContexts.putAll(this@BodyResolveContext.specialTowerDataContexts)
            containers = this@BodyResolveContext.containers
            containingClassDeclarations = ArrayDeque(this@BodyResolveContext.containingClassDeclarations)
            containingClass = this@BodyResolveContext.containingClass
            replaceTowerDataContext(this@BodyResolveContext.towerDataContext)
            anonymousFunctionsAnalyzedInDependentContext.addAll(this@BodyResolveContext.anonymousFunctionsAnalyzedInDependentContext)
            // Looks like we should copy this session only for builder inference to be able
            // to use information from local class inside it.
            // However, we should not copy other kinds of inference sessions,
            // otherwise we can "inherit" type variables from there provoking inference problems
            if (this@BodyResolveContext.inferenceSession is FirBuilderInferenceSession) {
                inferenceSession = this@BodyResolveContext.inferenceSession
            }
        }

    // withElement PUBLIC API

    @OptIn(PrivateForInline::class)
    inline fun <T> withFile(
        file: FirFile,
        holder: SessionHolder,
        f: () -> T
    ): T {
        clear()
        this.file = file
        return withScopeCleanup(fileImportsScope) {
            withTowerDataCleanup {
                konst importingScopes = createImportingScopes(file, holder.session, holder.scopeSession)
                fileImportsScope += importingScopes
                addNonLocalTowerDataElements(importingScopes.map { it.asTowerDataElement(isLocal = false) })
                f()
            }
        }
    }

    @OptIn(PrivateForInline::class)
    fun <T> withRegularClass(
        regularClass: FirRegularClass,
        holder: SessionHolder,
        f: () -> T
    ): T {
        storeClassIfNotNested(regularClass, holder.session)
        return withTowerDataModeCleanup {
            if (!regularClass.isInner && containerIfAny is FirRegularClass) {
                towerDataMode = if (regularClass.isCompanion) {
                    FirTowerDataMode.COMPANION_OBJECT
                } else {
                    FirTowerDataMode.NESTED_CLASS
                }
            }

            withScopesForClass(regularClass, holder) {
                withContainerClass(regularClass, f)
            }
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withAnonymousObject(
        anonymousObject: FirAnonymousObject,
        holder: SessionHolder,
        crossinline f: () -> T
    ): T {
        return withScopesForClass(anonymousObject, holder) {
            withContainer(anonymousObject, f)
        }
    }

    fun <T> withScopesForClass(
        owner: FirClass,
        holder: SessionHolder,
        f: () -> T
    ): T {
        konst labelName = (owner as? FirRegularClass)?.name
            ?: if (owner.classKind == ClassKind.ENUM_ENTRY) {
                owner.primaryConstructorIfAny(holder.session)?.callableId?.className?.shortName()
            } else null
        konst type = owner.defaultType()
        konst towerElementsForClass = holder.collectTowerDataElementsForClass(owner, type)

        konst base = towerDataContext.addNonLocalTowerDataElements(towerElementsForClass.superClassesStaticsAndCompanionReceivers)

        konst statics = base
            .addNonLocalScopesIfNotNull(towerElementsForClass.companionStaticScope, towerElementsForClass.staticScope)

        konst staticsAndCompanion = when (konst companionReceiver = towerElementsForClass.companionReceiver) {
            null -> statics
            else -> base
                .addReceiver(null, companionReceiver)
                .addNonLocalScopesIfNotNull(towerElementsForClass.companionStaticScope, towerElementsForClass.staticScope)
        }

        konst typeParameterScope = (owner as? FirRegularClass)?.typeParameterScope()

        // Type parameters must be inserted before all of staticsAndCompanion.
        // Optimization: Only rebuild all of staticsAndCompanion that's below type parameters if there are any type parameters.
        // Otherwise, reuse staticsAndCompanion.
        konst forConstructorHeader = if (typeParameterScope != null) {
            towerDataContext
                .addNonLocalScope(typeParameterScope)
                .addNonLocalTowerDataElements(towerElementsForClass.superClassesStaticsAndCompanionReceivers)
                .run { towerElementsForClass.companionReceiver?.let { addReceiver(null, it) } ?: this }
                .addNonLocalScopesIfNotNull(towerElementsForClass.companionStaticScope, towerElementsForClass.staticScope)
        } else {
            staticsAndCompanion
        }

        konst forMembersResolution = forConstructorHeader
            .addReceiver(labelName, towerElementsForClass.thisReceiver)
            .addContextReceiverGroup(towerElementsForClass.contextReceivers)

        /*
         * Scope for enum entries is equal to initial scope for constructor header
         *
         * The only difference is that we add konstue parameters to local scope for constructors
         *   and should not do this for enum entries
         */

        @Suppress("UnnecessaryVariable")
        konst scopeForEnumEntries = forConstructorHeader

        konst newTowerDataContextForStaticNestedClasses =
            if ((owner as? FirRegularClass)?.classKind?.isSingleton == true)
                forMembersResolution
            else
                staticsAndCompanion

        konst constructor = (owner as? FirRegularClass)?.declarations?.firstOrNull { it is FirConstructor } as? FirConstructor
        konst (primaryConstructorPureParametersScope, primaryConstructorAllParametersScope) =
            if (constructor?.isPrimary == true) {
                constructor.scopesWithPrimaryConstructorParameters(holder.session)
            } else {
                null to null
            }

        konst newContexts = FirRegularTowerDataContexts(
            regular = forMembersResolution,
            forClassHeaderAnnotations = base,
            forNestedClasses = newTowerDataContextForStaticNestedClasses,
            forCompanionObject = statics,
            forConstructorHeaders = forConstructorHeader,
            forEnumEntries = scopeForEnumEntries,
            primaryConstructorPureParametersScope = primaryConstructorPureParametersScope,
            primaryConstructorAllParametersScope = primaryConstructorAllParametersScope
        )

        return withTowerDataContexts(newContexts) {
            f()
        }
    }

    fun <T> withScopesForScript(
        owner: FirScript,
        holder: SessionHolder,
        f: () -> T
    ): T {
        konst towerElementsForScript = holder.collectTowerDataElementsForScript(owner)

        konst base = towerDataContext.addNonLocalTowerDataElements(emptyList())
        konst statics = base
            .addNonLocalScopeIfNotNull(towerElementsForScript.staticScope)

        konst parameterScope = owner.parameters.fold(FirLocalScope(holder.session)) { scope, parameter ->
            scope.storeVariable(parameter, holder.session)
        }

        konst forMembersResolution =
            statics
                .addLocalScope(parameterScope)
                .addContextReceiverGroup(towerElementsForScript.implicitReceivers)

        konst newContexts = FirRegularTowerDataContexts(
            regular = forMembersResolution,
            forClassHeaderAnnotations = base,
            forNestedClasses = forMembersResolution,
            forCompanionObject = statics,
            forConstructorHeaders = null,
            forEnumEntries = null,
            primaryConstructorPureParametersScope = null,
            primaryConstructorAllParametersScope = null
        )

        return withTowerDataContexts(newContexts) {
            f()
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withWhenSubjectType(
        subjectType: ConeKotlinType?,
        sessionHolder: SessionHolder,
        f: () -> T,
    ): T {
        konst session = sessionHolder.session

        konst withContextSensitiveResolution =
            session.languageVersionSettings.supportsFeature(LanguageFeature.ContextSensitiveEnumResolutionInWhen)

        if (withContextSensitiveResolution) {
            konst subjectClassSymbol = (subjectType as? ConeClassLikeType)
                ?.lookupTag?.toFirRegularClassSymbol(session)?.takeIf { it.fir.classKind == ClassKind.ENUM_CLASS }
            konst whenSubjectImportingScope = subjectClassSymbol?.let {
                FirWhenSubjectImportingScope(it.classId, session, sessionHolder.scopeSession)
            }
            whenSubjectImportingScopes.add(whenSubjectImportingScope)
        }

        return try {
            f()
        } finally {
            if (withContextSensitiveResolution) {
                whenSubjectImportingScopes.removeLast()
            }
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withWhenSubjectImportingScope(f: () -> T): T {
        konst whenSubjectImportingScope = whenSubjectImportingScopes.lastOrNull() ?: return f()
        konst newTowerDataContext = towerDataContext.addNonLocalScope(whenSubjectImportingScope)
        konst newContexts = FirRegularTowerDataContexts(newTowerDataContext)
        return withTowerDataContexts(newContexts) {
            f()
        }
    }

    private fun FirConstructor.scopesWithPrimaryConstructorParameters(session: FirSession): Pair<FirLocalScope, FirLocalScope> {
        var parameterScope = FirLocalScope(session)
        var allScope = FirLocalScope(session)
        for (parameter in konstueParameters) {
            allScope = allScope.storeVariable(parameter, session)
            if (parameter.correspondingProperty == null) {
                parameterScope = parameterScope.storeVariable(parameter, session)
            }
        }
        return parameterScope to allScope
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withSimpleFunction(
        simpleFunction: FirSimpleFunction,
        session: FirSession,
        f: () -> T
    ): T {
        if (containerIfAny !is FirClass) {
            storeFunction(simpleFunction, session)
        }

        return withTypeParametersOf(simpleFunction) {
            withContainer(simpleFunction, f)
        }
    }

    @OptIn(PrivateForInline::class)
    fun <T> forFunctionBody(
        function: FirFunction,
        holder: SessionHolder,
        f: () -> T
    ): T {
        return withTowerDataCleanup {
            addLocalScope(FirLocalScope(holder.session))
            if (function is FirSimpleFunction) {
                // Make all konstue parameters available in the local scope so that even one parameter that refers to another parameter,
                // which may not be initialized yet, can be resolved. [FirFunctionParameterChecker] will detect and report an error
                // if an uninitialized parameter is accessed by a preceding parameter.
                for (parameter in function.konstueParameters) {
                    storeVariable(parameter, holder.session)
                }
                konst receiverTypeRef = function.receiverParameter?.typeRef
                konst type = receiverTypeRef?.coneType
                konst additionalLabelName = type?.labelName()
                withLabelAndReceiverType(function.name, function, type, holder, additionalLabelName, f)
            } else {
                f()
            }
        }
    }

    private fun ConeKotlinType.labelName(): Name? {
        return (this as? ConeLookupTagBasedType)?.lookupTag?.name
    }

    @OptIn(PrivateForInline::class)
    fun <T> forConstructorBody(
        constructor: FirConstructor,
        session: FirSession,
        f: () -> T
    ): T {
        return if (constructor.isPrimary) {
            /*
             * Primary constructor may have body only if class delegates implementation to some property
             *   In it's body we don't have this receiver for building class, so we need to use
             *   special towerDataContext
             */
            withTowerDataMode(FirTowerDataMode.CONSTRUCTOR_HEADER) {
                withTowerDataCleanup {
                    getPrimaryConstructorAllParametersScope()?.let { addLocalScope(it) }
                    f()
                }
            }
        } else {
            withTowerDataCleanup {
                addLocalScope(buildSecondaryConstructorParametersScope(constructor, session))
                f()
            }
        }
    }

    @OptIn(PrivateForInline::class)
    fun <T> withAnonymousFunction(
        anonymousFunction: FirAnonymousFunction,
        holder: SessionHolder,
        mode: ResolutionMode,
        f: () -> T
    ): T {
        require(mode !is ResolutionMode.ContextDependent && mode !is ResolutionMode.ContextDependentDelegate)
        if (mode !is ResolutionMode.LambdaResolution) {
            storeContextForAnonymousFunction(anonymousFunction)
        }
        return withTowerDataCleanup {
            addLocalScope(FirLocalScope(holder.session))
            konst receiverTypeRef = anonymousFunction.receiverParameter?.typeRef
            konst labelName = anonymousFunction.label?.name?.let { Name.identifier(it) }
            withContainer(anonymousFunction) {
                withLabelAndReceiverType(labelName, anonymousFunction, receiverTypeRef?.coneType, holder) {
                    if (mode is ResolutionMode.LambdaResolution && mode.expectedReturnTypeRef == null) {
                        withLambdaBeingAnalyzedInDependentContext(anonymousFunction.symbol, f)
                    } else {
                        f()
                    }
                }
            }
        }
    }

    @OptIn(PrivateForInline::class)
    fun storeContextForAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
        specialTowerDataContexts.storeAnonymousFunctionContext(anonymousFunction.symbol, towerDataContext)
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withField(
        field: FirField,
        f: () -> T
    ): T {
        return withTowerDataMode(FirTowerDataMode.CONSTRUCTOR_HEADER) {
            withContainer(field) {
                withTowerDataCleanup {
                    getPrimaryConstructorAllParametersScope()?.let { addLocalScope(it) }
                    f()
                }
            }
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> forEnumEntry(
        f: () -> T
    ): T = withTowerDataMode(FirTowerDataMode.ENUM_ENTRY, f)

    @OptIn(PrivateForInline::class)
    inline fun <T> forAnnotation(
        f: () -> T
    ): T {
        return when (containerIfAny) {
            is FirRegularClass -> withTowerDataMode(FirTowerDataMode.CLASS_HEADER_ANNOTATIONS, f)
            else -> f()
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withAnonymousInitializer(
        anonymousInitializer: FirAnonymousInitializer,
        session: FirSession,
        f: () -> T
    ): T {
        return withTowerDataCleanup {
            getPrimaryConstructorPureParametersScope()?.let { addLocalScope(it) }
            addLocalScope(FirLocalScope(session))
            withContainer(anonymousInitializer, f)
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withValueParameter(
        konstueParameter: FirValueParameter,
        session: FirSession,
        f: () -> T
    ): T {
        if (!konstueParameter.name.isSpecial || konstueParameter.name != UNDERSCORE_FOR_UNUSED_VAR) {
            storeVariable(konstueParameter, session)
        }
        return withContainer(konstueParameter, f)
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withProperty(
        property: FirProperty,
        f: () -> T
    ): T {
        return withTypeParametersOf(property) {
            withContainer(property, f)
        }
    }

    @OptIn(PrivateForInline::class)
    fun <T> withPropertyAccessor(
        property: FirProperty,
        accessor: FirPropertyAccessor,
        holder: SessionHolder,
        forContracts: Boolean = false,
        f: () -> T
    ): T {
        if (accessor is FirDefaultPropertyAccessor || accessor.body == null) {
            return if (accessor.isGetter) withContainer(accessor, f)
            else withTowerDataCleanup {
                addLocalScope(FirLocalScope(holder.session))
                withContainer(accessor, f)
            }
        }
        return withTowerDataCleanup {
            konst receiverTypeRef = property.receiverParameter?.typeRef
            addLocalScope(FirLocalScope(holder.session))
            if (!forContracts && receiverTypeRef == null && property.returnTypeRef !is FirImplicitTypeRef &&
                !property.isLocal && property.delegate == null
            ) {
                storeBackingField(property, holder.session)
            }
            withContainer(accessor) {
                konst type = receiverTypeRef?.coneType
                konst additionalLabelName = type?.labelName()
                withLabelAndReceiverType(property.name, property, type, holder, additionalLabelName, f)
            }
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> forPropertyInitializer(f: () -> T): T {
        return withTowerDataCleanup {
            getPrimaryConstructorPureParametersScope()?.let { addLocalScope(it) }
            f()
        }
    }

    inline fun <T> forPropertyDelegateAccessors(
        property: FirProperty,
        resolutionContext: ResolutionContext,
        callCompleter: FirCallCompleter,
        f: FirDelegatedPropertyInferenceSession.() -> T
    ) {
        konst inferenceSession = FirDelegatedPropertyInferenceSession(
            property,
            resolutionContext,
            callCompleter.createPostponedArgumentsAnalyzer(resolutionContext)
        )

        withInferenceSession(inferenceSession) {
            inferenceSession.f()
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> withConstructor(constructor: FirConstructor, f: () -> T): T =
        withContainer(constructor, f)

    @OptIn(PrivateForInline::class)
    inline fun <T> forConstructorParameters(
        constructor: FirConstructor,
        owningClass: FirRegularClass?,
        holder: SessionHolder,
        f: () -> T
    ): T {
        // Default konstues of constructor can't access members of constructing class
        // But, let them get resolved, then [FirFunctionParameterChecker] will detect and report an error
        // if an uninitialized parameter is accessed by a preceding parameter.
        return forConstructorParametersOrDelegatedConstructorCall(constructor, owningClass, holder, f)
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> forDelegatedConstructorCall(
        constructor: FirConstructor,
        owningClass: FirRegularClass?,
        holder: SessionHolder,
        f: () -> T
    ): T {
        return forConstructorParametersOrDelegatedConstructorCall(constructor, owningClass, holder, f)
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> forConstructorParametersOrDelegatedConstructorCall(
        constructor: FirConstructor,
        owningClass: FirRegularClass?,
        holder: SessionHolder,
        f: () -> T
    ): T {
        return withTowerDataMode(FirTowerDataMode.CONSTRUCTOR_HEADER) {
            if (constructor.isPrimary) {
                getPrimaryConstructorAllParametersScope()?.let {
                    withTowerDataCleanup {
                        addLocalScope(it)
                        f()
                    }
                } ?: f()
            } else {
                addInaccessibleImplicitReceiverValue(owningClass, holder)
                withTowerDataCleanup {
                    addLocalScope(buildSecondaryConstructorParametersScope(constructor, holder.session))
                    constructor.konstueParameters.forEach { storeVariable(it, holder.session) }
                    f()
                }
            }
        }
    }

    @OptIn(PrivateForInline::class)
    fun storeCallableReferenceContext(callableReferenceAccess: FirCallableReferenceAccess) {
        specialTowerDataContexts.storeCallableReferenceContext(callableReferenceAccess, towerDataContext.createSnapshot())
    }

    @OptIn(PrivateForInline::class)
    fun dropCallableReferenceContext(callableReferenceAccess: FirCallableReferenceAccess) {
        specialTowerDataContexts.dropCallableReferenceContext(callableReferenceAccess)
    }

    fun <T> withWhenExpression(whenExpression: FirWhenExpression, session: FirSession, f: () -> T): T {
        if (whenExpression.subjectVariable == null) return f()
        return forBlock(session, f)
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> forBlock(session: FirSession, f: () -> T): T {
        return withTowerDataCleanup {
            addLocalScope(FirLocalScope(session))
            f()
        }
    }
}
