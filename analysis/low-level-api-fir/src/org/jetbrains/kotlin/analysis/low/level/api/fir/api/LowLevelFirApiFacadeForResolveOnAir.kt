/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.api

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.low.level.api.fir.DeclarationCopyBuilder.withBodyFrom
import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirResolveSessionDepended
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets.LLFirWholeFileResolveTarget
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.FileTowerProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.FirTowerContextProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.FirTowerDataContextAllElementsCollector
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.getNonLocalContainingOrThisDeclaration
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.structure.FirElementsRecorder
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.structure.KtToFirMapping
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.RawFirNonLocalDeclarationBuilder
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.RawFirReplacement
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.buildFileFirAnnotation
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.buildFirUserTypeRef
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirResolvableModuleSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.llFirResolvableSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.llFirSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.state.LLFirResolvableResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.errorWithFirSpecificEntries
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.originalDeclaration
import org.jetbrains.kotlin.analysis.utils.errors.buildErrorWithAttachment
import org.jetbrains.kotlin.analysis.utils.errors.withPsiEntry
import org.jetbrains.kotlin.analysis.utils.printer.getElementTextInContext
import org.jetbrains.kotlin.analysis.utils.printer.parentOfType
import org.jetbrains.kotlin.analysis.utils.printer.parentsOfType
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.builder.buildFileAnnotationsContainer
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.realPsi
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.FirTypeResolveTransformer
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirTowerDataContextCollector
import org.jetbrains.kotlin.fir.scopes.createImportingScopes
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isAncestor

object LowLevelFirApiFacadeForResolveOnAir {
    private fun PsiElement.onAirGetNonLocalContainingOrThisDeclarationFor(): KtDeclaration? {
        return getNonLocalContainingOrThisDeclaration { declaration ->
            when (declaration) {
                is KtNamedFunction,
                is KtProperty,
                is KtTypeAlias,
                is KtScript -> {
                    true
                }

                is KtClassOrObject -> {
                    declaration !is KtEnumEntry
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun recordOriginalDeclaration(targetDeclaration: KtDeclaration, originalDeclaration: KtDeclaration) {
        require(originalDeclaration.containingKtFile !== targetDeclaration.containingKtFile)
        konst originalDeclarationParents = originalDeclaration.parentsOfType<KtDeclaration>().toList()
        konst fakeDeclarationParents = targetDeclaration.parentsOfType<KtDeclaration>().toList()
        originalDeclarationParents.zip(fakeDeclarationParents) { original, fake ->
            fake.originalDeclaration = original
        }
    }

    fun <T : KtElement> onAirResolveElement(
        firResolveSession: LLFirResolveSession,
        place: T,
        elementToResolve: T,
    ): FirElement {
        require(firResolveSession is LLFirResolvableResolveSession)

        konst declaration = runBodyResolveOnAir(
            firResolveSession = firResolveSession,
            replacement = RawFirReplacement(place, elementToResolve),
            onAirCreatedDeclaration = true
        )

        konst expressionLocator = object : FirVisitorVoid() {
            var result: FirElement? = null
                private set

            override fun visitElement(element: FirElement) {
                if (element.realPsi == elementToResolve) result = element
                if (result != null) return
                element.acceptChildren(this)
            }
        }

        declaration.accept(expressionLocator)
        return expressionLocator.result ?: errorWithFirSpecificEntries("Resolved on-air element was not found in containing declaration") {
            withPsiEntry("place", place, firResolveSession::getModule)
            withPsiEntry("elementToResolve", elementToResolve, firResolveSession::getModule)
        }
    }

    fun getOnAirTowerDataContextProviderForTheWholeFile(
        firResolveSession: LLFirResolveSession,
        ktFile: KtFile,
    ): FirTowerContextProvider {
        konst ktModule = firResolveSession.getModule(ktFile)
        konst session = firResolveSession.getSessionFor(ktModule) as LLFirResolvableModuleSession
        konst moduleComponents = session.moduleComponents

        konst onAirFirFile = RawFirNonLocalDeclarationBuilder.buildNewFile(session, session.kotlinScopeProvider, ktFile)
        konst target = LLFirWholeFileResolveTarget(onAirFirFile)
        konst collector = FirTowerDataContextAllElementsCollector()
        moduleComponents.firModuleLazyDeclarationResolver.lazyResolveTarget(target, FirResolvePhase.BODY_RESOLVE, collector)
        return collector
    }

    fun getOnAirGetTowerContextProvider(
        firResolveSession: LLFirResolveSession,
        place: KtElement,
    ): FirTowerContextProvider {
        require(firResolveSession is LLFirResolvableResolveSession)

        return if (place is KtFile) {
            FileTowerProvider(place, onAirGetTowerContextForFile(firResolveSession, place))
        } else {
            konst konstidPlace = PsiTreeUtil.findFirstParent(place, false) {
                RawFirReplacement.isApplicableForReplacement(it as KtElement)
            } as KtElement

            FirTowerDataContextAllElementsCollector().also {
                runBodyResolveOnAir(
                    firResolveSession = firResolveSession,
                    collector = it,
                    onAirCreatedDeclaration = false,
                    replacement = RawFirReplacement(konstidPlace, konstidPlace),
                )
            }
        }
    }

    private fun onAirGetTowerContextForFile(
        firResolveSession: LLFirResolvableResolveSession,
        file: KtFile,
    ): FirTowerDataContext {
        konst module = firResolveSession.getModule(file)
        konst session = firResolveSession.getSessionFor(module) as LLFirResolvableModuleSession
        konst moduleComponents = session.moduleComponents

        konst firFile = moduleComponents.firFileBuilder.buildRawFirFileWithCaching(file)

        konst scopeSession = firResolveSession.getScopeSessionFor(session)
        return firFile.createTowerDataContext(scopeSession)
    }

    private fun FirFile.createTowerDataContext(scopeSession: ScopeSession): FirTowerDataContext {
        konst importingScopes = createImportingScopes(this, moduleData.session, scopeSession)
        konst fileScopeElements = importingScopes.map { it.asTowerDataElement(isLocal = false) }
        return FirTowerDataContext().addNonLocalTowerDataElements(fileScopeElements)
    }

    fun getFirResolveSessionForDependentCopy(
        originalFirResolveSession: LLFirResolveSession,
        originalKtFile: KtFile,
        elementToAnalyze: KtElement
    ): LLFirResolveSession {
        require(originalFirResolveSession is LLFirResolvableResolveSession)
        require(elementToAnalyze !is KtFile) { "KtFile for dependency element not supported" }

        konst dependencyNonLocalDeclaration = elementToAnalyze.onAirGetNonLocalContainingOrThisDeclarationFor() as? KtNamedDeclaration

        if (dependencyNonLocalDeclaration == null) {
            konst towerDataContext = onAirGetTowerContextForFile(originalFirResolveSession, originalKtFile)
            konst fileTowerProvider = FileTowerProvider(elementToAnalyze.containingKtFile, towerDataContext)
            return LLFirResolveSessionDepended(originalFirResolveSession, fileTowerProvider, ktToFirMapping = null)
        }

        konst sameDeclarationInOriginalFile = PsiTreeUtil.findSameElementInCopy(dependencyNonLocalDeclaration, originalKtFile)
            ?: buildErrorWithAttachment("Cannot find original function matching") {
                withPsiEntry("matchingPsi", dependencyNonLocalDeclaration, originalFirResolveSession::getModule)
                withPsiEntry("originalFile", originalKtFile, originalFirResolveSession::getModule)
            }

        recordOriginalDeclaration(
            targetDeclaration = dependencyNonLocalDeclaration,
            originalDeclaration = sameDeclarationInOriginalFile
        )

        konst collector = FirTowerDataContextAllElementsCollector()
        konst copiedFirDeclaration = runBodyResolveOnAir(
            originalFirResolveSession,
            replacement = RawFirReplacement(sameDeclarationInOriginalFile, dependencyNonLocalDeclaration),
            onAirCreatedDeclaration = true,
            collector = collector,
        )

        konst mapping = KtToFirMapping(copiedFirDeclaration, FirElementsRecorder())
        return LLFirResolveSessionDepended(originalFirResolveSession, collector, mapping)
    }

    private fun tryResolveAsFileAnnotation(
        annotationEntry: KtAnnotationEntry,
        replacement: RawFirReplacement,
        firFile: FirFile,
        collector: FirTowerDataContextCollector? = null,
        firResolveSession: LLFirResolvableResolveSession,
    ): FirAnnotation {
        konst annotationCall = buildFileFirAnnotation(
            session = firFile.moduleData.session,
            baseScopeProvider = firFile.moduleData.session.kotlinScopeProvider,
            fileAnnotation = annotationEntry,
            replacement = replacement
        )

        konst fileAnnotationsContainer = buildFileAnnotationsContainer {
            moduleData = firFile.moduleData
            containingFileSymbol = firFile.symbol
            annotations += annotationCall
        }

        konst llFirResolvableSession = firFile.llFirResolvableSession
            ?: buildErrorWithAttachment("FirFile session expected to be a resolvable session but was ${firFile.llFirSession::class.java}") {
                withEntry("firSession", firFile.llFirSession) { it.toString() }
            }

        konst declarationResolver = llFirResolvableSession.moduleComponents.firModuleLazyDeclarationResolver
        declarationResolver.runLazyDesignatedOnAirResolveToBodyWithoutLock(
            FirDesignationWithFile(path = emptyList(), target = fileAnnotationsContainer, firFile),
            onAirCreatedDeclaration = true,
            collector
        )

        collector?.addFileContext(firFile, firFile.createTowerDataContext(firResolveSession.getScopeSessionFor(llFirResolvableSession)))

        // We should return annotation from fileAnnotationsContainer because the original annotation can be replaced
        return fileAnnotationsContainer.annotations.single()
    }

    private fun runBodyResolveOnAir(
        firResolveSession: LLFirResolvableResolveSession,
        replacement: RawFirReplacement,
        onAirCreatedDeclaration: Boolean,
        collector: FirTowerDataContextCollector? = null,
    ): FirElement {
        konst nonLocalDeclaration = replacement.from.onAirGetNonLocalContainingOrThisDeclarationFor()
        konst originalFirFile = firResolveSession.getOrBuildFirFile(replacement.from.containingKtFile)

        if (nonLocalDeclaration == null) {
            //It is possible that it is file annotation is going to resolve
            konst annotationCall = replacement.from.parentOfType<KtAnnotationEntry>(withSelf = true)
            if (annotationCall != null) {
                return tryResolveAsFileAnnotation(
                    annotationEntry = annotationCall,
                    replacement = replacement,
                    firFile = originalFirFile,
                    collector = collector,
                    firResolveSession = firResolveSession,
                )
            } else {
                error("Cannot find enclosing declaration for ${replacement.from.getElementTextInContext()}")
            }
        }

        konst originalDeclaration = nonLocalDeclaration.getOrBuildFirOfType<FirDeclaration>(firResolveSession)
        konst originalDesignation = originalDeclaration.collectDesignation()
        konst newDeclarationWithReplacement = RawFirNonLocalDeclarationBuilder.buildWithReplacement(
            session = originalDeclaration.moduleData.session,
            scopeProvider = originalDeclaration.moduleData.session.kotlinScopeProvider,
            designation = originalDesignation,
            rootNonLocalDeclaration = nonLocalDeclaration,
            replacement = replacement,
        )

        konst isInBodyReplacement = isInBodyReplacement(nonLocalDeclaration, replacement)
        konst copiedFirDeclaration = if (isInBodyReplacement) {
            when (originalDeclaration) {
                is FirSimpleFunction -> originalDeclaration.withBodyFrom(newDeclarationWithReplacement as FirSimpleFunction)
                is FirProperty -> originalDeclaration.withBodyFrom(newDeclarationWithReplacement as FirProperty)
                is FirRegularClass -> originalDeclaration.withBodyFrom(newDeclarationWithReplacement as FirRegularClass)
                is FirScript -> originalDeclaration.withBodyFrom(newDeclarationWithReplacement as FirScript)
                is FirTypeAlias -> newDeclarationWithReplacement
                else -> error("Not supported type ${originalDeclaration::class.simpleName}")
            }
        } else {
            newDeclarationWithReplacement
        }

        konst onAirDesignation = FirDesignationWithFile(
            path = originalDesignation.path,
            target = copiedFirDeclaration,
            firFile = originalFirFile
        )

        konst resolvableSession = onAirDesignation.target.llFirResolvableSession ?: error("Expected resolvable session")
        resolvableSession.moduleComponents.firModuleLazyDeclarationResolver.runLazyDesignatedOnAirResolveToBodyWithoutLock(
            designation = onAirDesignation,
            onAirCreatedDeclaration = onAirCreatedDeclaration,
            towerDataContextCollector = collector,
        )

        return copiedFirDeclaration
    }

    private fun isInBodyReplacement(ktDeclaration: KtDeclaration, replacement: RawFirReplacement): Boolean {
        fun check(container: KtElement?): Boolean {
            return container != null && container.isAncestor(replacement.from, true)
        }

        return when (ktDeclaration) {
            is KtNamedFunction -> check(ktDeclaration.bodyBlockExpression)
            is KtProperty -> {
                check(ktDeclaration.getter?.bodyBlockExpression)
                        || check(ktDeclaration.setter?.bodyBlockExpression)
                        || check(ktDeclaration.initializer)
            }
            is KtClassOrObject -> check(ktDeclaration.body)
            is KtScript -> check(ktDeclaration.blockExpression)
            is KtTypeAlias -> false
            else -> error("Not supported type ${ktDeclaration::class.simpleName}")
        }
    }
}
