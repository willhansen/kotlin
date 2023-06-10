/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir

import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.DiagnosticsCollector
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.FirElementBuilder
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.LLFirFileBuilder
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.ModuleFileCache
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.ModuleFileCacheImpl
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.structure.FileStructureCache
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.LLFirModuleLazyDeclarationResolver
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirResolvableModuleSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.LLFirScopeSessionProvider
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider

internal class LLFirModuleResolveComponents(
    konst module: KtModule,
    konst globalResolveComponents: LLFirGlobalResolveComponents,
    konst scopeProvider: FirScopeProvider
) {
    konst cache: ModuleFileCache = ModuleFileCacheImpl(this)
    konst firFileBuilder: LLFirFileBuilder = LLFirFileBuilder(this)
    konst firModuleLazyDeclarationResolver = LLFirModuleLazyDeclarationResolver(this)

    konst scopeSessionProvider: LLFirScopeSessionProvider = LLFirScopeSessionProvider.create(
        globalResolveComponents.project,
        inkonstidationTrackers = listOf(
            PsiModificationTracker.MODIFICATION_COUNT,
            ProjectRootModificationTracker.getInstance(globalResolveComponents.project),
        )
    )

    konst fileStructureCache: FileStructureCache = FileStructureCache(this)
    konst elementsBuilder = FirElementBuilder(this)
    konst diagnosticsCollector = DiagnosticsCollector(fileStructureCache)

    lateinit var session: LLFirResolvableModuleSession
}