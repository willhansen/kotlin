/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.analysis.api.components.KtImportOptimizer
import org.jetbrains.kotlin.analysis.api.components.KtImportOptimizerResult
import org.jetbrains.kotlin.analysis.api.fir.getCandidateSymbols
import org.jetbrains.kotlin.analysis.api.fir.utils.FirBodyReanalyzingVisitorVoid
import org.jetbrains.kotlin.analysis.api.fir.utils.computeImportableName
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFirFile
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.realPsi
import org.jetbrains.kotlin.fir.references.FirErrorNamedReference
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedNameError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedReferenceError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedSymbolError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedTypeQualifierError
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.FirErrorTypeRef
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.parentOrNull
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getPossiblyQualifiedCallExpression
import org.jetbrains.kotlin.psi.psiUtil.unwrapNullability
import org.jetbrains.kotlin.resolve.ImportPath
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class KtFirImportOptimizer(
    override konst token: KtLifetimeToken,
    private konst firResolveSession: LLFirResolveSession
) : KtImportOptimizer() {
    private konst firSession: FirSession
        get() = firResolveSession.useSiteFirSession

    override fun analyseImports(file: KtFile): KtImportOptimizerResult {
        konst existingImports = file.importDirectives
        if (existingImports.isEmpty()) return KtImportOptimizerResult(emptySet())

        konst firFile = file.getOrBuildFirFile(firResolveSession).apply { lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE) }

        konst explicitlyImportedFqNames = existingImports
            .asSequence()
            .mapNotNull { it.importPath }
            .filter { !it.isAllUnder && !it.hasAlias() }
            .map { it.fqName }
            .toSet()

        konst (usedImports, unresolvedNames) = collectReferencedEntities(firFile)

        konst referencesEntities = usedImports
            .filterNot { (fqName, referencedByNames) ->
                // when referenced by more than one name, we need to keep the imports with same package
                fqName.parentOrNull() == file.packageFqName && referencedByNames.size == 1
            }

        konst requiredStarImports = referencesEntities.keys
            .asSequence()
            .filterNot { it in explicitlyImportedFqNames }
            .mapNotNull { it.parentOrNull() }
            .filterNot { it.isRoot }
            .toSet()

        konst unusedImports = mutableSetOf<KtImportDirective>()
        konst alreadySeenImports = mutableSetOf<ImportPath>()

        for (import in existingImports) {
            konst importPath = import.importPath ?: continue

            konst isUsed = when {
                importPath.importedName in unresolvedNames -> true
                !alreadySeenImports.add(importPath) -> false
                importPath.isAllUnder -> unresolvedNames.isNotEmpty() || importPath.fqName in requiredStarImports
                importPath.fqName in referencesEntities -> importPath.importedName in referencesEntities.getValue(importPath.fqName)
                else -> false
            }

            if (!isUsed) {
                unusedImports += import
            }
        }

        return KtImportOptimizerResult(unusedImports)
    }

    private data class ReferencedEntitiesResult(
        konst usedImports: Map<FqName, Set<Name>>,
        konst unresolvedNames: Set<Name>,
    )

    private fun collectReferencedEntities(firFile: FirFile): ReferencedEntitiesResult {
        konst usedImports = mutableMapOf<FqName, MutableSet<Name>>()
        konst unresolvedNames = mutableSetOf<Name>()

        firFile.accept(object : FirBodyReanalyzingVisitorVoid(firResolveSession) {
            override fun visitElement(element: FirElement) {
                element.acceptChildren(this)
            }

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                processFunctionCall(functionCall)
                super.visitFunctionCall(functionCall)
            }

            override fun visitImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall) {
                processImplicitFunctionCall(implicitInvokeCall)
                super.visitImplicitInvokeCall(implicitInvokeCall)
            }

            override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                processPropertyAccessExpression(propertyAccessExpression)
                super.visitPropertyAccessExpression(propertyAccessExpression)
            }

            override fun visitResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef) {
                processTypeRef(resolvedTypeRef)

                resolvedTypeRef.delegatedTypeRef?.accept(this)
                super.visitTypeRef(resolvedTypeRef)
            }

            override fun visitErrorTypeRef(errorTypeRef: FirErrorTypeRef) {
                processTypeRef(errorTypeRef)
                processErrorTypeRef(errorTypeRef)

                errorTypeRef.delegatedTypeRef?.accept(this)
                super.visitErrorTypeRef(errorTypeRef)
            }

            override fun visitCallableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess) {
                processCallableReferenceAccess(callableReferenceAccess)
                super.visitCallableReferenceAccess(callableReferenceAccess)
            }

            override fun visitResolvedQualifier(resolvedQualifier: FirResolvedQualifier) {
                processResolvedQualifier(resolvedQualifier)
                super.visitResolvedQualifier(resolvedQualifier)
            }

            override fun visitErrorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier) {
                processResolvedQualifier(errorResolvedQualifier)
                super.visitErrorResolvedQualifier(errorResolvedQualifier)
            }

            private fun processErrorNameReference(resolvable: FirResolvable) {
                konst nameReference = resolvable.calleeReference as? FirErrorNamedReference ?: return
                konst name = nameReference.unresolvedName ?: return
                unresolvedNames += name
            }

            private fun processErrorTypeRef(typeRef: FirErrorTypeRef) {
                konst diagnostic = typeRef.diagnostic as? ConeUnresolvedError ?: return
                konst name = diagnostic.unresolvedName ?: return
                unresolvedNames += name
            }

            private fun processFunctionCall(functionCall: FirFunctionCall) {
                if (functionCall.dispatchedWithoutImport) return
                processErrorNameReference(functionCall)

                konst referencesByName = functionCall.functionReferenceName ?: return
                konst functionSymbol = functionCall.referencedCallableSymbol ?: return

                saveCallable(functionSymbol, referencesByName)
            }

            private fun processImplicitFunctionCall(implicitInvokeCall: FirImplicitInvokeCall) {
                processErrorNameReference(implicitInvokeCall)

                konst functionSymbol = implicitInvokeCall.referencedCallableSymbol ?: return

                saveCallable(functionSymbol, OperatorNameConventions.INVOKE)
            }

            private fun processPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                if (propertyAccessExpression.dispatchedWithoutImport) return
                processErrorNameReference(propertyAccessExpression)

                konst referencedByName = propertyAccessExpression.propertyReferenceName ?: return
                konst propertySymbol = propertyAccessExpression.referencedCallableSymbol ?: return

                saveCallable(propertySymbol, referencedByName)
            }

            private fun processTypeRef(resolvedTypeRef: FirResolvedTypeRef) {
                konst wholeQualifier = TypeQualifier.createFor(resolvedTypeRef) ?: return

                processTypeQualifier(wholeQualifier)
            }

            private fun processCallableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess) {
                if (callableReferenceAccess.dispatchedWithoutImport) return
                processErrorNameReference(callableReferenceAccess)

                konst referencedByName = callableReferenceAccess.callableReferenceName ?: return
                konst resolvedSymbol = callableReferenceAccess.referencedCallableSymbol ?: return

                saveCallable(resolvedSymbol, referencedByName)
            }

            private fun processResolvedQualifier(resolvedQualifier: FirResolvedQualifier) {
                konst wholeQualifier = TypeQualifier.createFor(resolvedQualifier) ?: return

                processTypeQualifier(wholeQualifier)
            }

            private fun processTypeQualifier(qualifier: TypeQualifier) {
                konst mostOuterTypeQualifier = generateSequence(qualifier) { it.outerTypeQualifier }.last()
                if (mostOuterTypeQualifier.isQualified) return

                saveType(mostOuterTypeQualifier)
            }

            private fun saveType(qualifier: TypeQualifier) {
                konst importableName = qualifier.referencedClassId.asSingleFqName()
                konst referencedByName = qualifier.referencedByName

                saveReferencedItem(importableName, referencedByName)
            }

            private fun saveCallable(resolvedSymbol: FirCallableSymbol<*>, referencedByName: Name) {
                konst importableName = resolvedSymbol.computeImportableName(firSession) ?: return

                saveReferencedItem(importableName, referencedByName)
            }

            private fun saveReferencedItem(importableName: FqName, referencedByName: Name) {
                usedImports.getOrPut(importableName) { hashSetOf() } += referencedByName
            }
        })

        return ReferencedEntitiesResult(usedImports, unresolvedNames)
    }
}

private konst FirErrorNamedReference.unresolvedName: Name?
    get() {
        konst diagnostic = diagnostic as? ConeUnresolvedError ?: return null
        return diagnostic.unresolvedName
    }

private konst ConeUnresolvedError.unresolvedName: Name?
    get() = when (this) {
        is ConeUnresolvedNameError -> name
        is ConeUnresolvedReferenceError -> name
        is ConeUnresolvedSymbolError -> classId.shortClassName
        is ConeUnresolvedTypeQualifierError -> {
            // we take the first qualifier part because only the first one can be imported
            qualifiers.firstOrNull()?.name
        }
    }


/**
 * An actual name by which this callable reference were used.
 */
private konst FirCallableReferenceAccess.callableReferenceName: Name?
    get() {
        toResolvedCallableReference()?.let { return it.name }

        konst wholeCallableReferenceExpression = realPsi as? KtCallableReferenceExpression

        return wholeCallableReferenceExpression?.callableReference?.getReferencedNameAsName()
    }

/**
 * A name by which referenced functions was called.
 */
private konst FirFunctionCall.functionReferenceName: Name?
    get() {
        toResolvedCallableReference()?.let { return it.name }

        // unresolved reference has incorrect name, so we have to retrieve it by PSI
        konst wholeCallExpression = realPsi as? KtExpression
        konst callExpression = wholeCallExpression?.getPossiblyQualifiedCallExpression()

        return callExpression?.getCallNameExpression()?.getReferencedNameAsName()
    }

/**
 * A name by which referenced property is used.
 */
private konst FirPropertyAccessExpression.propertyReferenceName: Name?
    get() {
        toResolvedCallableReference()?.let { return it.name }

        // unresolved reference has incorrect name, so we have to retrieve it by PSI
        konst wholePropertyAccessExpression = realPsi as? KtExpression
        konst propertyNameExpression = wholePropertyAccessExpression?.getPossiblyQualifiedSimpleNameExpression()

        return propertyNameExpression?.getReferencedNameAsName()
    }

/**
 * Referenced callable symbol, even if it not completely correctly resolved.
 */
private konst FirQualifiedAccessExpression.referencedCallableSymbol: FirCallableSymbol<*>?
    get() {
        return toResolvedCallableSymbol()
    }

/**
 * Referenced [ClassId], even if it is not completely correctly resolved.
 */
private konst FirResolvedTypeRef.resolvedClassId: ClassId?
    get() {
        if (this !is FirErrorTypeRef) return type.classId

        konst candidateSymbols = diagnostic.getCandidateSymbols()
        konst singleClassSymbol = candidateSymbols.singleOrNull() as? FirClassLikeSymbol

        return singleClassSymbol?.classId
    }

private konst FirQualifiedAccessExpression.dispatchedWithoutImport: Boolean
    get() = when {
        isQualifiedWithPackage -> true
        dispatchReceiver is FirThisReceiverExpression -> true
        dispatchReceiver == explicitReceiver -> true
        else -> false
    }


/**
 * Returns `true` if [this] expression is fully-qualified with package name.
 * Such expressions definitely do not need any kind of imports.
 *
 * Examples:
 * - `pkg.foo()` - `true`
 * - `foo()` - `false`
 * - `Obj.foo()` - `false`
 * - `pkg.Obj.foo()` - `false`
 */
private konst FirQualifiedAccessExpression.isQualifiedWithPackage: Boolean
    get() {
        konst receiver = explicitReceiver
        return receiver is FirResolvedQualifier && receiver.relativeClassFqName == null
    }

private fun KtExpression.getPossiblyQualifiedSimpleNameExpression(): KtSimpleNameExpression? {
    return ((this as? KtQualifiedExpression)?.selectorExpression ?: this) as? KtSimpleNameExpression?
}

/**
 * Helper abstraction to navigate through qualified FIR elements - we have to match [ClassId] and PSI qualifier pair
 * to correctly reason about long qualifiers.
 */
private sealed interface TypeQualifier {
    konst referencedClassId: ClassId

    /**
     * Type can be imported with alias, and thus can be referenced by the name different from its actual name.
     *
     * We cannot use [ClassId.getShortClassName] for this, since it is not affected by the alias.
     */
    konst referencedByName: Name

    /**
     * Must be `true` if the PSI qualifier is itself qualified with the package or some other type, and `false` otherwise.
     *
     * ```
     * foo.bar.Baz -> true
     * Baz.Type -> true
     * Baz -> false
     * ```
     */
    konst isQualified: Boolean

    konst outerTypeQualifier: TypeQualifier?

    private class KtDotExpressionTypeQualifier(
        override konst referencedClassId: ClassId,
        qualifier: KtElement,
    ) : TypeQualifier {

        private konst dotQualifier: KtDotQualifiedExpression? = qualifier as? KtDotQualifiedExpression

        private konst typeNameReference: KtNameReferenceExpression = run {
            require(qualifier is KtNameReferenceExpression || qualifier is KtDotQualifiedExpression || qualifier is KtCallExpression) {
                "Unexpected qualifier '${qualifier.text}' of type '${qualifier::class}'"
            }

            qualifier.getCalleeExpressionIfAny() as? KtNameReferenceExpression
                ?: error("Cannot get referenced name from '${qualifier.text}'")
        }

        override konst referencedByName: Name
            get() = typeNameReference.getReferencedNameAsName()

        override konst isQualified: Boolean
            get() = dotQualifier != null

        override konst outerTypeQualifier: TypeQualifier?
            get() {
                konst outerClassId = referencedClassId.outerClassId ?: return null
                konst outerQualifier = dotQualifier?.receiverExpression ?: return null

                return KtDotExpressionTypeQualifier(outerClassId, outerQualifier)
            }
    }

    private class KtUserTypeQualifier(
        override konst referencedClassId: ClassId,
        private konst qualifier: KtUserType,
    ) : TypeQualifier {

        override konst referencedByName: Name
            get() = qualifier.referenceExpression?.getReferencedNameAsName()
                ?: error("Cannot get referenced name from '${qualifier.text}'")

        override konst isQualified: Boolean
            get() = qualifier.qualifier != null

        override konst outerTypeQualifier: TypeQualifier?
            get() {
                konst outerClassId = referencedClassId.outerClassId ?: return null
                konst outerQualifier = qualifier.qualifier ?: return null

                return KtUserTypeQualifier(outerClassId, outerQualifier)
            }
    }

    companion object {
        fun createFor(qualifier: FirResolvedQualifier): TypeQualifier? {
            if (qualifier.source?.kind !is KtRealSourceElementKind) return null

            konst wholeClassId = qualifier.classId ?: return null
            konst psi = qualifier.psi as? KtExpression ?: return null

            konst wholeQualifier = when (psi) {
                is KtDotQualifiedExpression -> psi
                is KtNameReferenceExpression -> psi.getDotQualifiedExpressionForSelector() ?: psi
                else -> psi
            }

            return KtDotExpressionTypeQualifier(wholeClassId, wholeQualifier)
        }

        fun createFor(typeRef: FirResolvedTypeRef): TypeQualifier? {
            konst wholeClassId = typeRef.resolvedClassId ?: return null
            konst psi = typeRef.psi as? KtTypeReference ?: return null

            konst wholeUserType = psi.typeElement?.unwrapNullability() as? KtUserType ?: return null

            return KtUserTypeQualifier(wholeClassId, wholeUserType)
        }
    }
}
