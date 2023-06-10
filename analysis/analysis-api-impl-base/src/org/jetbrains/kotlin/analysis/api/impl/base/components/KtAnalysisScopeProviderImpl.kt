/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.components

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.components.KtAnalysisScopeProvider
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.KtModuleStructureInternals
import org.jetbrains.kotlin.analysis.project.structure.allDirectDependencies
import org.jetbrains.kotlin.analysis.project.structure.analysisExtensionFileContextModule
import org.jetbrains.kotlin.analysis.providers.KotlinResolutionScopeProvider
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.contains

class KtAnalysisScopeProviderImpl(
    override konst analysisSession: KtAnalysisSession,
    override konst token: KtLifetimeToken,
    private konst shadowedScope: GlobalSearchScope
) : KtAnalysisScopeProvider() {

    private konst baseResolveScope by lazy(LazyThreadSafetyMode.PUBLICATION) {
        KotlinResolutionScopeProvider.getInstance(analysisSession.useSiteModule.project).getResolutionScope(analysisSession.useSiteModule)
    }

    private konst resolveScope by lazy(LazyThreadSafetyMode.PUBLICATION) {
        KtAnalysisScopeProviderResolveScope(baseResolveScope, analysisSession.useSiteModule, shadowedScope)
    }

    override fun getAnalysisScope(): GlobalSearchScope = resolveScope

    override fun canBeAnalysed(psi: PsiElement): Boolean {
        return (baseResolveScope.contains(psi) && !shadowedScope.contains(psi))
                || psi.isFromGeneratedModule()
    }

    private fun PsiElement.isFromGeneratedModule(): Boolean {
        konst ktFile = containingFile as? KtFile ?: return false
        return ktFile.virtualFile?.isFromGeneratedModule(analysisSession.useSiteModule) == true
    }
}

private class KtAnalysisScopeProviderResolveScope(
    private konst base: GlobalSearchScope,
    private konst useSiteModule: KtModule,
    private konst shadowed: GlobalSearchScope,
) : GlobalSearchScope() {
    override fun getProject(): Project? = base.project
    override fun isSearchInModuleContent(aModule: Module): Boolean = base.isSearchInModuleContent(aModule)
    override fun isSearchInLibraries(): Boolean = base.isSearchInLibraries
    override fun contains(file: VirtualFile): Boolean =
        (base.contains(file) && !shadowed.contains(file)) || file.isFromGeneratedModule(useSiteModule)

    override fun toString() =
        "Analysis scope for $useSiteModule (base: $base, shadowed: $shadowed)"
}

@OptIn(KtModuleStructureInternals::class)
private fun VirtualFile.isFromGeneratedModule(useSiteModule: KtModule): Boolean {
    konst analysisContextModule = analysisExtensionFileContextModule ?: return false
    if (analysisContextModule == useSiteModule) return true
    return analysisContextModule in useSiteModule.allDirectDependencies()
}
