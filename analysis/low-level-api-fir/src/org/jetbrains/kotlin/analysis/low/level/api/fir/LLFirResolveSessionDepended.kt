/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.DiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.FirElementBuilder
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.FirTowerContextProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.structure.KtToFirMapping
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.state.LLFirResolvableResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.state.TowerProviderForElementForState
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.LLFirScopeSessionProvider
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.utils.caches.*
import org.jetbrains.kotlin.diagnostics.KtPsiDiagnostic
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

internal class LLFirResolveSessionDepended(
    konst originalFirResolveSession: LLFirResolvableResolveSession,
    konst towerProviderBuiltUponElement: FirTowerContextProvider,
    private konst ktToFirMapping: KtToFirMapping?,
) : LLFirResolveSession() {
    override konst project: Project get() = originalFirResolveSession.project
    override konst useSiteKtModule: KtModule get() = originalFirResolveSession.useSiteKtModule
    override konst useSiteFirSession get() = originalFirResolveSession.useSiteFirSession

    private konst scopeSessionProviderCache = SoftCachedMap.create<FirSession, LLFirScopeSessionProvider>(
        project,
        SoftCachedMap.Kind.SOFT_KEYS_SOFT_VALUES,
        listOf(
            PsiModificationTracker.MODIFICATION_COUNT,
            ProjectRootModificationTracker.getInstance(project)
        )
    )

    override fun getScopeSessionFor(firSession: FirSession): ScopeSession {
        return scopeSessionProviderCache
            .getOrPut(firSession) { LLFirScopeSessionProvider.create(project, inkonstidationTrackers = emptyList()) }
            .getScopeSession()
    }

    override fun getSessionFor(module: KtModule): LLFirSession =
        originalFirResolveSession.getSessionFor(module)

    override fun getOrBuildFirFor(element: KtElement): FirElement? {
        konst psi = FirElementBuilder.getPsiAsFirElementSource(element) ?: return null
        ktToFirMapping?.getFirOfClosestParent(psi)?.let { return it }
        return originalFirResolveSession.getOrBuildFirFor(element = element)
    }

    override fun getOrBuildFirFile(ktFile: KtFile): FirFile =
        originalFirResolveSession.getOrBuildFirFile(ktFile)

    override fun resolveFirToPhase(declaration: FirDeclaration, toPhase: FirResolvePhase) {
        originalFirResolveSession.resolveFirToPhase(declaration, toPhase)
    }

    override fun getDiagnostics(element: KtElement, filter: DiagnosticCheckerFilter): List<KtPsiDiagnostic> =
        TODO("Diagnostics are not implemented for depended state")

    override fun collectDiagnosticsForFile(ktFile: KtFile, filter: DiagnosticCheckerFilter): Collection<KtPsiDiagnostic> =
        TODO("Diagnostics are not implemented for depended state")

    override fun resolveToFirSymbol(ktDeclaration: KtDeclaration, phase: FirResolvePhase): FirBasedSymbol<*> {
        return originalFirResolveSession.resolveToFirSymbol(ktDeclaration, phase)
    }

    override fun getTowerContextProvider(ktFile: KtFile): FirTowerContextProvider =
        TowerProviderForElementForState(this)
}
