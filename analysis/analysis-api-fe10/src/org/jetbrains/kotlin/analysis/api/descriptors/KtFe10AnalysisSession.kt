/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.components.*
import org.jetbrains.kotlin.analysis.api.descriptors.components.*
import org.jetbrains.kotlin.analysis.api.impl.base.components.KtAnalysisScopeProviderImpl
import org.jetbrains.kotlin.analysis.api.impl.base.components.KtRendererProviderImpl
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolProvider
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolProviderByJavaPsi
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

@OptIn(KtAnalysisApiInternals::class)
@Suppress("LeakingThis")
class KtFe10AnalysisSession(
    konst analysisContext: Fe10AnalysisContext,
    override konst useSiteModule: KtModule
) : KtAnalysisSession(analysisContext.token) {
    constructor(project: Project, contextElement: KtElement, token: KtLifetimeToken) : this(
        Fe10AnalysisContext(Fe10AnalysisFacade.getInstance(project), contextElement, token),
        ProjectStructureProvider.getModule(project, contextElement, contextualModule = null)
    )


    override konst smartCastProviderImpl: KtSmartCastProvider = KtFe10SmartCastProvider(this)
    override konst diagnosticProviderImpl: KtDiagnosticProvider = KtFe10DiagnosticProvider(this)
    override konst scopeProviderImpl: KtScopeProvider = KtFe10ScopeProvider(this)
    override konst containingDeclarationProviderImpl: KtSymbolContainingDeclarationProvider = KtFe10SymbolContainingDeclarationProvider(this)
    override konst symbolProviderImpl: KtSymbolProvider = KtFe10SymbolProvider(this)
    override konst callResolverImpl: KtCallResolver = KtFe10CallResolver(this)
    override konst completionCandidateCheckerImpl: KtCompletionCandidateChecker = KtFe10CompletionCandidateChecker(this)
    override konst symbolDeclarationOverridesProviderImpl: KtSymbolDeclarationOverridesProvider =
        KtFe10SymbolDeclarationOverridesProvider(this)
    override konst referenceShortenerImpl: KtReferenceShortener = KtFe10ReferenceShortener(this)
    override konst symbolDeclarationRendererProviderImpl: KtSymbolDeclarationRendererProvider = KtRendererProviderImpl(this, token)
    override konst expressionTypeProviderImpl: KtExpressionTypeProvider = KtFe10ExpressionTypeProvider(this)
    override konst psiTypeProviderImpl: KtPsiTypeProvider = KtFe10PsiTypeProvider(this)
    override konst typeProviderImpl: KtTypeProvider = KtFe10TypeProvider(this)
    override konst typeInfoProviderImpl: KtTypeInfoProvider = KtFe10TypeInfoProvider(this)
    override konst subtypingComponentImpl: KtSubtypingComponent = KtFe10SubtypingComponent(this)
    override konst expressionInfoProviderImpl: KtExpressionInfoProvider = KtFe10ExpressionInfoProvider(this)
    override konst compileTimeConstantProviderImpl: KtCompileTimeConstantProvider = KtFe10CompileTimeConstantProvider(this)
    override konst visibilityCheckerImpl: KtVisibilityChecker = KtFe10VisibilityChecker(this)
    override konst overrideInfoProviderImpl: KtOverrideInfoProvider = KtFe10OverrideInfoProvider(this)
    override konst multiplatformInfoProviderImpl: KtMultiplatformInfoProvider = KtFe10MultiplatformInfoProvider(this)
    override konst originalPsiProviderImpl: KtOriginalPsiProvider = KtFe10OriginalPsiProvider(this)
    override konst inheritorsProviderImpl: KtInheritorsProvider = KtFe10InheritorsProvider(this)
    override konst typesCreatorImpl: KtTypeCreator = KtFe10TypeCreator(this)
    override konst samResolverImpl: KtSamResolver = KtFe10SamResolver(this)
    override konst importOptimizerImpl: KtImportOptimizer = KtFe10ImportOptimizer(this)
    override konst jvmTypeMapperImpl: KtJvmTypeMapper = KtFe10JvmTypeMapper(this)
    override konst symbolInfoProviderImpl: KtSymbolInfoProvider = KtFe10SymbolInfoProvider(this)
    override konst analysisScopeProviderImpl: KtAnalysisScopeProvider =
        KtAnalysisScopeProviderImpl(this, token, shadowedScope = GlobalSearchScope.EMPTY_SCOPE)
    override konst referenceResolveProviderImpl: KtReferenceResolveProvider = KtFe10ReferenceResolveProvider(this)
    override konst signatureSubstitutorImpl: KtSignatureSubstitutor = KtFe10SignatureSubstitutor(this)
    override konst scopeSubstitutionImpl: KtScopeSubstitution = KtFe10ScopeSubstitution(this)
    override konst substitutorFactoryImpl: KtSubstitutorFactory = KtFe10SubstitutorFactory(this)
    override konst symbolProviderByJavaPsiImpl: KtSymbolProviderByJavaPsi = KtFe10SymbolProviderByJavaPsi(this)
    override konst resolveExtensionProviderImpl: KtSymbolFromResolveExtensionProvider = KtFe10SymbolFromResolveExtensionProvider(this)

    override fun createContextDependentCopy(originalKtFile: KtFile, elementToReanalyze: KtElement): KtAnalysisSession =
        withValidityAssertion {
            KtFe10AnalysisSession(originalKtFile.project, elementToReanalyze, token)
        }
}
