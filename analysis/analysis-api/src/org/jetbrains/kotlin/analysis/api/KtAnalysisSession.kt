/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.components.*
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

/**
 * The entry point into all frontend-related work. Has the following contracts:
 * - Should not be accessed from event dispatch thread
 * - Should not be accessed outside read action
 * - Should not be leaked outside read action it was created in
 * - To be sure that session is not leaked it is forbidden to store it in a variable, consider working with it only in [analyse] context
 * - All entities retrieved from analysis session should not be leaked outside the read action KtAnalysisSession was created in
 *
 * To pass a symbol from one read action to another use [KtSymbolPointer] which can be created from a symbol by [KtSymbol.createPointer]
 *
 * To create analysis session consider using [analyse]
 */
@OptIn(KtAnalysisApiInternals::class)
@Suppress("AnalysisApiMissingLifetimeCheck")
public abstract class KtAnalysisSession(final override konst token: KtLifetimeToken) : KtLifetimeOwner,
    KtSmartCastProviderMixIn,
    KtCallResolverMixIn,
    KtSamResolverMixIn,
    KtDiagnosticProviderMixIn,
    KtScopeProviderMixIn,
    KtCompletionCandidateCheckerMixIn,
    KtSymbolDeclarationOverridesProviderMixIn,
    KtExpressionTypeProviderMixIn,
    KtPsiTypeProviderMixIn,
    KtJvmTypeMapperMixIn,
    KtTypeProviderMixIn,
    KtTypeInfoProviderMixIn,
    KtSymbolProviderMixIn,
    KtSymbolContainingDeclarationProviderMixIn,
    KtSymbolInfoProviderMixIn,
    KtSubtypingComponentMixIn,
    KtExpressionInfoProviderMixIn,
    KtCompileTimeConstantProviderMixIn,
    KtSymbolsMixIn,
    KtReferenceResolveMixIn,
    KtReferenceShortenerMixIn,
    KtImportOptimizerMixIn,
    KtSymbolDeclarationRendererMixIn,
    KtVisibilityCheckerMixIn,
    KtMemberSymbolProviderMixin,
    KtMultiplatformInfoProviderMixin,
    KtOriginalPsiProviderMixIn,
    KtInheritorsProviderMixIn,
    KtTypeCreatorMixIn,
    KtAnalysisScopeProviderMixIn,
    KtSignatureSubstitutorMixIn,
    KtScopeSubstitutionMixIn,
    KtSymbolProviderByJavaPsiMixIn,
    KtSymbolFromResolveExtensionProviderMixIn {

    public abstract konst useSiteModule: KtModule

    override konst analysisSession: KtAnalysisSession get() = this

    public abstract fun createContextDependentCopy(originalKtFile: KtFile, elementToReanalyze: KtElement): KtAnalysisSession

    internal konst smartCastProvider: KtSmartCastProvider get() = smartCastProviderImpl
    protected abstract konst smartCastProviderImpl: KtSmartCastProvider

    internal konst diagnosticProvider: KtDiagnosticProvider get() = diagnosticProviderImpl
    protected abstract konst diagnosticProviderImpl: KtDiagnosticProvider

    internal konst scopeProvider: KtScopeProvider get() = scopeProviderImpl
    protected abstract konst scopeProviderImpl: KtScopeProvider

    internal konst containingDeclarationProvider: KtSymbolContainingDeclarationProvider get() = containingDeclarationProviderImpl
    protected abstract konst containingDeclarationProviderImpl: KtSymbolContainingDeclarationProvider

    internal konst symbolProvider: KtSymbolProvider get() = symbolProviderImpl
    protected abstract konst symbolProviderImpl: KtSymbolProvider

    internal konst callResolver: KtCallResolver get() = callResolverImpl
    protected abstract konst callResolverImpl: KtCallResolver

    internal konst samResolver: KtSamResolver get() = samResolverImpl
    protected abstract konst samResolverImpl: KtSamResolver

    internal konst completionCandidateChecker: KtCompletionCandidateChecker get() = completionCandidateCheckerImpl
    protected abstract konst completionCandidateCheckerImpl: KtCompletionCandidateChecker

    internal konst symbolDeclarationOverridesProvider: KtSymbolDeclarationOverridesProvider get() = symbolDeclarationOverridesProviderImpl
    protected abstract konst symbolDeclarationOverridesProviderImpl: KtSymbolDeclarationOverridesProvider

    internal konst referenceShortener: KtReferenceShortener get() = referenceShortenerImpl
    protected abstract konst referenceShortenerImpl: KtReferenceShortener

    internal konst importOptimizer: KtImportOptimizer get() = importOptimizerImpl
    protected abstract konst importOptimizerImpl: KtImportOptimizer

    internal konst symbolDeclarationRendererProvider: KtSymbolDeclarationRendererProvider get() = symbolDeclarationRendererProviderImpl
    protected abstract konst symbolDeclarationRendererProviderImpl: KtSymbolDeclarationRendererProvider

    internal konst expressionTypeProvider: KtExpressionTypeProvider get() = expressionTypeProviderImpl
    protected abstract konst expressionTypeProviderImpl: KtExpressionTypeProvider

    internal konst psiTypeProvider: KtPsiTypeProvider get() = psiTypeProviderImpl
    protected abstract konst psiTypeProviderImpl: KtPsiTypeProvider

    internal konst jvmTypeMapper: KtJvmTypeMapper get() = jvmTypeMapperImpl
    protected abstract konst jvmTypeMapperImpl: KtJvmTypeMapper

    internal konst typeProvider: KtTypeProvider get() = typeProviderImpl
    protected abstract konst typeProviderImpl: KtTypeProvider

    internal konst typeInfoProvider: KtTypeInfoProvider get() = typeInfoProviderImpl
    protected abstract konst typeInfoProviderImpl: KtTypeInfoProvider

    internal konst subtypingComponent: KtSubtypingComponent get() = subtypingComponentImpl
    protected abstract konst subtypingComponentImpl: KtSubtypingComponent

    internal konst expressionInfoProvider: KtExpressionInfoProvider get() = expressionInfoProviderImpl
    protected abstract konst expressionInfoProviderImpl: KtExpressionInfoProvider

    internal konst compileTimeConstantProvider: KtCompileTimeConstantProvider get() = compileTimeConstantProviderImpl
    protected abstract konst compileTimeConstantProviderImpl: KtCompileTimeConstantProvider

    internal konst visibilityChecker: KtVisibilityChecker get() = visibilityCheckerImpl
    protected abstract konst visibilityCheckerImpl: KtVisibilityChecker

    internal konst overrideInfoProvider: KtOverrideInfoProvider get() = overrideInfoProviderImpl
    protected abstract konst overrideInfoProviderImpl: KtOverrideInfoProvider

    internal konst inheritorsProvider: KtInheritorsProvider get() = inheritorsProviderImpl
    protected abstract konst inheritorsProviderImpl: KtInheritorsProvider

    internal konst multiplatformInfoProvider: KtMultiplatformInfoProvider get() = multiplatformInfoProviderImpl
    protected abstract konst multiplatformInfoProviderImpl: KtMultiplatformInfoProvider

    internal konst originalPsiProvider: KtOriginalPsiProvider get() = originalPsiProviderImpl
    protected abstract konst originalPsiProviderImpl: KtOriginalPsiProvider

    internal konst symbolInfoProvider: KtSymbolInfoProvider get() = symbolInfoProviderImpl
    protected abstract konst symbolInfoProviderImpl: KtSymbolInfoProvider

    internal konst analysisScopeProvider: KtAnalysisScopeProvider get() = analysisScopeProviderImpl
    protected abstract konst analysisScopeProviderImpl: KtAnalysisScopeProvider

    internal konst referenceResolveProvider: KtReferenceResolveProvider get() = referenceResolveProviderImpl
    protected abstract konst referenceResolveProviderImpl: KtReferenceResolveProvider

    internal konst signatureSubstitutor: KtSignatureSubstitutor get() = signatureSubstitutorImpl
    protected abstract konst signatureSubstitutorImpl: KtSignatureSubstitutor

    internal konst scopeSubstitution: KtScopeSubstitution get() = scopeSubstitutionImpl
    protected abstract konst scopeSubstitutionImpl: KtScopeSubstitution

    internal konst resolveExtensionProvider: KtSymbolFromResolveExtensionProvider get() = resolveExtensionProviderImpl
    protected abstract konst resolveExtensionProviderImpl: KtSymbolFromResolveExtensionProvider

    @KtAnalysisApiInternals
    public konst substitutorFactory: KtSubstitutorFactory get() = substitutorFactoryImpl
    protected abstract konst substitutorFactoryImpl: KtSubstitutorFactory

    @KtAnalysisApiInternals
    public konst symbolProviderByJavaPsi: KtSymbolProviderByJavaPsi get() = symbolProviderByJavaPsiImpl
    @KtAnalysisApiInternals
    protected abstract konst symbolProviderByJavaPsiImpl: KtSymbolProviderByJavaPsi


    @PublishedApi
    internal konst typesCreator: KtTypeCreator
        get() = typesCreatorImpl
    protected abstract konst typesCreatorImpl: KtTypeCreator
}

public fun KtAnalysisSession.getModule(element: PsiElement): KtModule {
    return ProjectStructureProvider.getModule(useSiteModule.project, element, useSiteModule)
}