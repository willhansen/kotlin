/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.components.*
import org.jetbrains.kotlin.analysis.api.fir.components.*
import org.jetbrains.kotlin.analysis.api.fir.symbols.KtFirOverrideInfoProvider
import org.jetbrains.kotlin.analysis.api.fir.symbols.KtFirSymbolProvider
import org.jetbrains.kotlin.analysis.api.impl.base.components.KtAnalysisScopeProviderImpl
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LowLevelFirApiFacadeForResolveOnAir
import org.jetbrains.kotlin.analysis.providers.impl.declarationProviders.CompositeKotlinDeclarationProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure.CompositeKotlinPackageProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.resolve.extensions.LLFirResolveExtensionTool
import org.jetbrains.kotlin.analysis.low.level.api.fir.resolve.extensions.llResolveExtensionTool
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.allDirectDependencies
import org.jetbrains.kotlin.analysis.providers.KotlinDeclarationProvider
import org.jetbrains.kotlin.analysis.providers.KotlinPackageProvider
import org.jetbrains.kotlin.analysis.providers.createDeclarationProvider
import org.jetbrains.kotlin.analysis.providers.createPackageProvider
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.addIfNotNull

@OptIn(KtAnalysisApiInternals::class)
@Suppress("AnalysisApiMissingLifetimeCheck")
internal class KtFirAnalysisSession
private constructor(
    konst project: Project,
    konst firResolveSession: LLFirResolveSession,
    token: KtLifetimeToken,
    private konst mode: AnalysisSessionMode,
) : KtAnalysisSession(token) {

    internal konst firSymbolBuilder: KtSymbolByFirBuilder = KtSymbolByFirBuilder(project, this, token)

    @Suppress("AnalysisApiMissingLifetimeCheck")
    override konst useSiteModule: KtModule get() = firResolveSession.useSiteKtModule

    private enum class AnalysisSessionMode {
        REGULAR,
        DEPENDENT_COPY
    }

    override konst smartCastProviderImpl = KtFirSmartcastProvider(this, token)

    override konst expressionTypeProviderImpl = KtFirExpressionTypeProvider(this, token)

    override konst diagnosticProviderImpl = KtFirDiagnosticProvider(this, token)

    override konst containingDeclarationProviderImpl = KtFirSymbolContainingDeclarationProvider(this, token)

    override konst callResolverImpl = KtFirCallResolver(this, token)

    override konst samResolverImpl = KtFirSamResolver(this, token)

    override konst scopeProviderImpl = KtFirScopeProvider(this, firSymbolBuilder, firResolveSession)

    override konst symbolProviderImpl =
        KtFirSymbolProvider(this, firResolveSession.useSiteFirSession.symbolProvider)

    override konst completionCandidateCheckerImpl = KtFirCompletionCandidateChecker(this, token)

    override konst symbolDeclarationOverridesProviderImpl =
        KtFirSymbolDeclarationOverridesProvider(this, token)

    override konst referenceShortenerImpl = KtFirReferenceShortener(this, token, firResolveSession)

    override konst importOptimizerImpl: KtImportOptimizer = KtFirImportOptimizer(token, firResolveSession)

    override konst symbolDeclarationRendererProviderImpl: KtSymbolDeclarationRendererProvider = KtFirRendererProvider(this, token)

    override konst expressionInfoProviderImpl = KtFirExpressionInfoProvider(this, token)

    override konst compileTimeConstantProviderImpl: KtCompileTimeConstantProvider = KtFirCompileTimeConstantProvider(this, token)

    override konst overrideInfoProviderImpl = KtFirOverrideInfoProvider(this, token)

    override konst visibilityCheckerImpl: KtVisibilityChecker = KtFirVisibilityChecker(this, token)

    override konst psiTypeProviderImpl = KtFirPsiTypeProvider(this, token)

    override konst jvmTypeMapperImpl = KtFirJvmTypeMapper(this, token)

    override konst typeProviderImpl = KtFirTypeProvider(this, token)

    override konst typeInfoProviderImpl = KtFirTypeInfoProvider(this, token)

    override konst subtypingComponentImpl = KtFirSubtypingComponent(this, token)

    override konst inheritorsProviderImpl: KtInheritorsProvider = KtFirInheritorsProvider(this, token)

    override konst multiplatformInfoProviderImpl: KtMultiplatformInfoProvider = KtFirMultiplatformInfoProvider(this, token)

    override konst originalPsiProviderImpl: KtOriginalPsiProvider = KtFirOriginalPsiProvider(this, token)

    override konst symbolInfoProviderImpl: KtSymbolInfoProvider = KtFirSymbolInfoProvider(this, token)

    override konst typesCreatorImpl: KtTypeCreator = KtFirTypeCreator(this, token)

    override konst analysisScopeProviderImpl: KtAnalysisScopeProvider

    override konst referenceResolveProviderImpl: KtReferenceResolveProvider = KtFirReferenceResolveProvider(this)

    override konst signatureSubstitutorImpl: KtSignatureSubstitutor = KtFirSignatureSubstitutor(this)

    override konst scopeSubstitutionImpl: KtScopeSubstitution = KtFirScopeSubstitution(this)

    override konst substitutorFactoryImpl: KtSubstitutorFactory = KtFirSubstitutorFactory(this)

    override konst symbolProviderByJavaPsiImpl = KtFirSymbolProviderByJavaPsi(this)

    override konst resolveExtensionProviderImpl: KtSymbolFromResolveExtensionProvider = KtFirSymbolFromResolveExtensionProvider(this)

    @Suppress("AnalysisApiMissingLifetimeCheck")
    override fun createContextDependentCopy(originalKtFile: KtFile, elementToReanalyze: KtElement): KtAnalysisSession {
        check(mode == AnalysisSessionMode.REGULAR) {
            "Cannot create context-dependent copy of KtAnalysis session from a context dependent one"
        }
        require(!elementToReanalyze.isPhysical) { "Depended context should be build only for non-physical elements" }

        konst contextFirResolveSession = LowLevelFirApiFacadeForResolveOnAir.getFirResolveSessionForDependentCopy(
            originalFirResolveSession = firResolveSession,
            originalKtFile = originalKtFile,
            elementToAnalyze = elementToReanalyze
        )

        return KtFirAnalysisSession(
            project,
            contextFirResolveSession,
            token,
            AnalysisSessionMode.DEPENDENT_COPY
        )
    }

    internal konst useSiteSession: FirSession get() = firResolveSession.useSiteFirSession
    internal konst firSymbolProvider: FirSymbolProvider get() = useSiteSession.symbolProvider
    internal konst targetPlatform: TargetPlatform get() = useSiteSession.moduleData.platform

    konst extensionTools: List<LLFirResolveExtensionTool>

    konst useSiteAnalysisScope: GlobalSearchScope

    konst useSiteScopeDeclarationProvider: KotlinDeclarationProvider
    konst useSitePackageProvider: KotlinPackageProvider


    init {
        extensionTools = buildList {
            addIfNotNull(useSiteSession.llResolveExtensionTool)
            useSiteModule.allDirectDependencies().mapNotNullTo(this) { dependency ->
                firResolveSession.getSessionFor(dependency).llResolveExtensionTool
            }
        }

        konst shadowedScope = GlobalSearchScope.union(
            buildSet {
                // Add an empty scope to the shadowed set to give GlobalSearchScope.union something
                // to work with if there are no extension tools.
                // If there are extension tools, any empty scopes, whether from shadowedSearchScope
                // on the extension tools or from this add() call, will be ignored.
                add(GlobalSearchScope.EMPTY_SCOPE)
                extensionTools.mapTo(this) { it.shadowedSearchScope }
            }
        )
        analysisScopeProviderImpl = KtAnalysisScopeProviderImpl(this, token, shadowedScope)
        useSiteAnalysisScope = analysisScopeProviderImpl.getAnalysisScope()

        useSiteScopeDeclarationProvider = CompositeKotlinDeclarationProvider.create(
            buildList {
                add(project.createDeclarationProvider(useSiteAnalysisScope, useSiteModule))
                extensionTools.mapTo(this) { it.declarationProvider }
            }
        )

        useSitePackageProvider = CompositeKotlinPackageProvider.create(
            buildList {
                add(project.createPackageProvider(useSiteAnalysisScope))
                extensionTools.mapTo(this) { it.packageProvider }
            }
        )
    }

    fun getScopeSessionFor(session: FirSession): ScopeSession = withValidityAssertion { firResolveSession.getScopeSessionFor(session) }

    companion object {
        internal fun createAnalysisSessionByFirResolveSession(
            firResolveSession: LLFirResolveSession,
            token: KtLifetimeToken,
        ): KtFirAnalysisSession {
            konst project = firResolveSession.project

            return KtFirAnalysisSession(
                project,
                firResolveSession,
                token,
                AnalysisSessionMode.REGULAR,
            )
        }
    }
}
